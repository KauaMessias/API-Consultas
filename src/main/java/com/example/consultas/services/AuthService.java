package com.example.consultas.services;

import com.example.consultas.dtos.auth.AuthTokens;
import com.example.consultas.dtos.auth.AuthenticationDto;
import com.example.consultas.dtos.auth.SenhaDto;
import com.example.consultas.exceptions.*;
import com.example.consultas.models.RefreshToken;
import com.example.consultas.models.StatusValidacao;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.models.ValidacaoEmailModel;
import com.example.consultas.repositories.RefreshTokenRepository;
import com.example.consultas.repositories.UsuarioRepository;
import com.example.consultas.repositories.ValidacaoEmailRepository;
import com.example.consultas.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    @Value("${ROTA_CONFIRMAR_EMAIL}")
    private String rotaConfirmar;
    @Value("${ROTA_RESTAURAR_SENHA}")
    private String rotaRestaurar;

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final ValidacaoEmailRepository validacaoEmailRepository;
    private final EmailService emailService;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService, RefreshTokenRepository refreshTokenRepository, UsuarioRepository usuarioRepository, ValidacaoEmailRepository validacaoEmailRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.validacaoEmailRepository = validacaoEmailRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthTokens login(AuthenticationDto authDto) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(authDto.email(), authDto.senha());
        Authentication auth = authenticationManager.authenticate(usernamePassword);
        UsuarioModel usuario = (UsuarioModel) auth.getPrincipal();
        String accessToken = tokenService.gerarAccessToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        RefreshToken refreshModel = criarRefresh(refreshToken, usuario);

        refreshTokenRepository.save(refreshModel);

        return new AuthTokens(accessToken, refreshToken, auth.getAuthorities().toString());
    }

    @Transactional
    public AuthTokens renovarToken(String token) {
        tokenService.validarRefreshToken(token);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(DigestUtils.sha256Hex(token)).orElseThrow(RefreshTokenNotFoundException::new);

        if (refreshToken.isRevoked()) {
            refreshTokenRepository.revokeAllByUsuarioId(refreshToken.getUsuario().getId());
            throw new RefreshTokenRevokedException();
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        UsuarioModel usuario = refreshToken.getUsuario();
        String accessToken = tokenService.gerarAccessToken(usuario);
        String newRefreshToken = tokenService.gerarRefreshToken(usuario);

        refreshTokenRepository.save(criarRefresh(newRefreshToken, usuario));

        return new AuthTokens(accessToken, newRefreshToken, usuario.getAuthorities().toString());
    }

    @Transactional
    public void revogarToken(String token) {
        tokenService.validarRefreshToken(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(DigestUtils.sha256Hex(token)).orElseThrow(RefreshTokenNotFoundException::new);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void enviarValidacao(UsuarioModel usuario) {
        ValidacaoEmailModel validacao = validacaoEmailRepository.save(new ValidacaoEmailModel(usuario, LocalDateTime.now().plusHours(24), StatusValidacao.PENDENTE));
        emailService.enviarEmail(usuario.getEmail(), "Cadastro de conta", "Confirme sua conta: " + rotaConfirmar + "/" + validacao.getToken());

    }

    @Transactional
    public void reenviarValidacao(UUID tokenAtivacao) {
        ValidacaoEmailModel validacao = validacaoEmailRepository.findById(tokenAtivacao).orElseThrow(TokenValidacaoNotFoundException::new);
        UsuarioModel usuario = validacao.getUsuario();
        if (!validacao.getStatus().equals(StatusValidacao.USADO) && !usuario.isEnabled()) {
            validacao.setStatus(StatusValidacao.CANCELADO);
            ValidacaoEmailModel novaValidacao = validacaoEmailRepository.save(new ValidacaoEmailModel(usuario, LocalDateTime.now().plusHours(24), StatusValidacao.PENDENTE));
            emailService.enviarEmail(usuario.getEmail(), "Cadastro de conta", "Confirme sua conta: " + rotaConfirmar + "/" + novaValidacao.getToken());
        }
    }

    @Transactional(noRollbackFor = TokenValidacaoExpiradoException.class)
    public void validarCadastro(UUID tokenAtivacao) {

        ValidacaoEmailModel validacao = validacaoEmailRepository.findById(tokenAtivacao).orElseThrow(TokenValidacaoNotFoundException::new);

        if (validacao.getStatus().equals(StatusValidacao.USADO)) {
            throw new UsuarioValidacaoException("Token já utilizado.");
        }

        if (validacao.getExpiresAt().isBefore(LocalDateTime.now())) {
            validacao.setStatus(StatusValidacao.EXPIRADO);
            throw new TokenValidacaoExpiradoException("Token expirado.");
        }
        UsuarioModel usuario = validacao.getUsuario();

        if (validacao.getStatus().equals(StatusValidacao.PENDENTE) && !usuario.isEnabled()) {
            usuario.setEnabled(true);
            validacao.setStatus(StatusValidacao.USADO);
        } else {
            validacao.setStatus(StatusValidacao.CANCELADO);
            throw new UsuarioValidacaoException(validacao.getStatus().toString());
        }
    }

    @Transactional
    public void alterarSenha(UsuarioModel usuario, SenhaDto senha) {
        if (!usuario.isEnabled()) throw new UsuarioInativoException();

        if (!passwordEncoder.matches(senha.senhaAtual(), usuario.getSenha())) throw new SenhaIncorretaException();

        usuario.setSenha(passwordEncoder.encode(senha.senha()));

        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());
    }

    @Transactional(noRollbackFor = {TokenValidacaoExpiradoException.class, UsuarioValidacaoException.class})
    public void restaurarSenha(UUID id, SenhaDto senha) {

        ValidacaoEmailModel validacao = validacaoEmailRepository.findById(id).orElseThrow(TokenValidacaoNotFoundException::new);

        if (validacao.getStatus().equals(StatusValidacao.USADO)) {
            throw new UsuarioValidacaoException("Token já utilizado.");
        }

        if (validacao.getExpiresAt().isBefore(LocalDateTime.now())) {
            validacao.setStatus(StatusValidacao.EXPIRADO);
            throw new TokenValidacaoExpiradoException("Token expirado");
        }

        UsuarioModel usuario = validacao.getUsuario();

        if (validacao.getStatus().equals(StatusValidacao.PENDENTE) && usuario.isEnabled()) {
            usuario.setSenha(passwordEncoder.encode(senha.senha()));
            usuarioRepository.save(usuario);
            validacao.setStatus(StatusValidacao.USADO);
            validacaoEmailRepository.save(validacao);
            refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());
        } else {

            validacao.setStatus(StatusValidacao.CANCELADO);
            throw new UsuarioValidacaoException("Token inválido");
        }
    }

    public void enviarRestauracao(String email) {
        Optional<UserDetails> usuarioOptional = usuarioRepository.findByEmail(email);

        if (usuarioOptional.isEmpty()) {
            return;
        }
        UsuarioModel usuario = (UsuarioModel) usuarioOptional.get();

        ValidacaoEmailModel novaValidacao = validacaoEmailRepository.save(new ValidacaoEmailModel(usuario, LocalDateTime.now().plusHours(24), StatusValidacao.PENDENTE));
        emailService.enviarEmail(usuario.getEmail(), "Restauração de senha", "Confirme seu email: " + rotaRestaurar + "/" + novaValidacao.getToken());
    }


    private RefreshToken criarRefresh(String refreshToken, UsuarioModel usuario) {
        return new RefreshToken(DigestUtils.sha256Hex(refreshToken), usuario, false, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
    }

}
