package com.example.consultas.services;

import com.example.consultas.dtos.ClienteRequestDto;
import com.example.consultas.dtos.ClienteResponseDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ClienteResponseDto addCliente(ClienteRequestDto clienteRequestDto) {

        if (usuarioRepository.existsByEmail(clienteRequestDto.email())) {
            throw new EntityExistsException("Email já cadastrado.");
        }

        if (clienteRepository.existsByCpf(clienteRequestDto.cpf())) {
            throw new EntityExistsException("CPF já cadastrado.");
        }

        ClienteModel clienteModel = new ClienteModel();
        BeanUtils.copyProperties(clienteRequestDto, clienteModel, "usuario", "senha", "email");

        UsuarioModel usuario = usuarioRepository.save(new UsuarioModel(clienteRequestDto.email(), passwordEncoder.encode(clienteRequestDto.senha()), Roles.CLIENTE));
        clienteModel.setUsuario(usuario);

        return new ClienteResponseDto(clienteRepository.save(clienteModel));
    }


    @Transactional
    public ClienteResponseDto updateCliente(UUID id, ClienteRequestDto clienteRequestDto) {
        ClienteModel clienteModel = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);
        UsuarioModel usuario = clienteModel.getUsuario();

        if (!clienteRequestDto.email().equals(usuario.getEmail())) {
            if(usuarioRepository.existsByEmail(clienteRequestDto.email())) {
                throw new EntityExistsException("Email já cadastrado.");
            }
            usuario.setEmail(clienteRequestDto.email());

        }

        if (clienteRequestDto.senha() != null && !clienteRequestDto.senha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(clienteRequestDto.senha()));
        }

        BeanUtils.copyProperties(clienteRequestDto, clienteModel, "usuario", "senha", "email", "cpf");

        usuarioRepository.save(usuario);

        return new ClienteResponseDto(clienteRepository.save(clienteModel));
    }


    public ClienteResponseDto getClienteById(UUID id) {
        return new ClienteResponseDto(clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new));
    }


    public Page<ClienteResponseDto> getClienteByNome(Pageable pageable, String nome) {
        return clienteRepository.findByNomeContainingIgnoreCase(pageable, nome).map(ClienteResponseDto::new);
    }


    public Page<ClienteResponseDto> getAllClientes(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(ClienteResponseDto::new);
    }


    @Transactional
    public void deleteCliente(UUID id) {
        ClienteModel cliente = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);
        UsuarioModel usuario = cliente.getUsuario();

        clienteRepository.delete(cliente);
        usuarioRepository.delete(usuario);
    }

}
