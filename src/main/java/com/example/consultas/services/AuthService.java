package com.example.consultas.services;

import com.example.consultas.dtos.auth.AuthTokens;
import com.example.consultas.dtos.auth.AuthenticationDto;
import com.example.consultas.exceptions.RefreshTokenNotFoundException;
import com.example.consultas.exceptions.RefreshTokenRevokedException;
import com.example.consultas.models.RefreshToken;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.RefreshTokenRepository;
import com.example.consultas.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthenticationManager authenticationManager, TokenService tokenService, RefreshTokenRepository refreshTokenRepository){
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthTokens login(AuthenticationDto authDto){
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
    public AuthTokens renovarToken(String token){
        tokenService.validarRefreshToken(token);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(DigestUtils.sha256Hex(token)).orElseThrow(RefreshTokenNotFoundException::new);

        if(refreshToken.isRevoked()){
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

    public void revogarToken(String token){
        tokenService.validarRefreshToken(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(DigestUtils.sha256Hex(token)).orElseThrow(RefreshTokenNotFoundException::new);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    private RefreshToken criarRefresh(String refreshToken, UsuarioModel usuario){
        return new RefreshToken(DigestUtils.sha256Hex(refreshToken), usuario, false, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
    }

}
