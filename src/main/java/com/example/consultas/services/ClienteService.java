package com.example.consultas.services;

import com.example.consultas.dtos.cliente.ClienteRequestDto;
import com.example.consultas.dtos.cliente.ClienteResponseDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.UsuarioInativoException;
import com.example.consultas.models.*;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.UsuarioRepository;
import com.example.consultas.repositories.ValidacaoEmailRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.UUID;

@Service
@Slf4j
public class ClienteService {

    private final AuthService authService;

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;
    private final EmailService emailService;
    private final ValidacaoEmailRepository verificacaoEmailRepository;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, ConsultaRepository consultaRepository, EmailService emailService, ValidacaoEmailRepository verificacaoEmailRepository, AuthService authService) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
        this.emailService = emailService;
        this.verificacaoEmailRepository = verificacaoEmailRepository;
        this.authService = authService;
    }

    @Transactional
    public ClienteResponseDto addCliente(ClienteRequestDto clienteRequestDto) {
        log.info("Criando um cliente");

        validarEmail(clienteRequestDto.email());

        if (clienteRepository.existsByCpf(clienteRequestDto.cpf())) {
            log.warn("CPF já está cadastrado");
            throw new EntityExistsException("CPF já cadastrado.");
        }

        ClienteModel clienteModel = clienteRequestDto.toEntity();

        UsuarioModel usuario = usuarioRepository.save(new UsuarioModel(clienteRequestDto.email(), passwordEncoder.encode(clienteRequestDto.senha()), Roles.CLIENTE, false));
        clienteModel.setUsuario(usuario);

        clienteModel = clienteRepository.save(clienteModel);

        authService.enviarValidacao(usuario);

        log.info("Cliente cadastrado com sucesso. id = {}", clienteModel.getId());

        return new ClienteResponseDto(clienteModel);
    }


    @Transactional
    public ClienteResponseDto updateCliente(UUID id, ClienteRequestDto clienteRequestDto) {
        log.info("Atualizando o cliente com id {}", id);
        ClienteModel clienteModel = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);
        UsuarioModel usuario = clienteModel.getUsuario();

        if (!usuario.isEnabled()) {
            log.warn("Usuário inativo");
            throw new UsuarioInativoException();
        }

        if (clienteRequestDto.email() != null && !clienteRequestDto.email().equals(usuario.getEmail())) {
            validarEmail(clienteRequestDto.email());
            usuario.setEmail(clienteRequestDto.email());
        }

        if (clienteRequestDto.senha() != null && !clienteRequestDto.senha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(clienteRequestDto.senha()));
        }

        usuarioRepository.save(usuario);

        clienteModel = clienteRepository.save(clienteRequestDto.updateEntity(clienteModel));
        log.info("Cliente atualizado com sucesso");

        return new ClienteResponseDto(clienteModel);
    }


    public ClienteResponseDto getClienteById(UUID id) {
        log.info("Buscando o cliente com o id {}", id);

        ClienteModel clienteModel = clienteRepository.findById(id).orElseThrow(() -> {
            log.warn("Cliente com o id {} não encontrado", id);
            return new ClienteNotFoundException();
        });

        log.info("Cliente encontrado com sucesso");
        return new ClienteResponseDto(clienteModel);
    }


    public Page<ClienteResponseDto> getClienteByNome(Pageable pageable, String nome) {
        log.info("Buscando os clientes com o nome {}", nome);
        var clientes = clienteRepository.findByNomeContainingIgnoreCase(pageable, nome).map(ClienteResponseDto::new);

        log.info("{} clientes encontrados", clientes.getTotalElements());
        return clientes;

    }


    public Page<ClienteResponseDto> getAllClientes(Pageable pageable) {
        log.info("Buscando todos os clientes");
        var clientes = clienteRepository.findAll(pageable).map(ClienteResponseDto::new);

        log.info("{} clientes encontrados", clientes.getTotalElements());
        return clientes;
    }


    @Transactional
    public void desativarCliente(UUID id) {
        ClienteModel cliente = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);
        UsuarioModel usuario = cliente.getUsuario();

        log.info("Desativando cliente com o id {}", cliente.getId());

        var consultas = consultaRepository.findByCliente_IdAndStatusNot(cliente.getId(), Status.CANCELADA);

        consultas.forEach(consulta -> {
            if (consulta.getStatus().equals(Status.PENDENTE)) consulta.setStatus(Status.CANCELADA);
        });

        usuario.setEnabled(false);

        log.info("Cliente desativado com sucesso");
    }

    private void validarEmail(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            log.warn("Email já cadastrado");
            throw new EntityExistsException("Email já cadastrado.");
        }
    }

    public ClienteResponseDto exibirPerfil(UsuarioModel usuarioModel) {
        if (!usuarioModel.getRole().equals(Roles.CLIENTE)) {
            throw new ResourceAccessException("Acesso negado");
        }

        return new ClienteResponseDto(clienteRepository.findByUsuario_Id(usuarioModel.getId()).orElseThrow(ClienteNotFoundException::new));
    }

}
