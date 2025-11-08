package com.example.consultas.services;

import com.example.consultas.dtos.ClienteRequestDto;
import com.example.consultas.dtos.ClienteResponseDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.repositories.ClienteRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ClienteResponseDto addCliente(ClienteRequestDto clienteRequestDto){
        if(clienteRepository.existsByEmail(clienteRequestDto.email())){
            throw new EntityExistsException("Email já cadastrado.");
        }

        if(clienteRepository.existsByCpf(clienteRequestDto.cpf())){
            throw new EntityExistsException("CPF já cadastrado.");
        }

        ClienteModel clienteModel = new ClienteModel();
        BeanUtils.copyProperties(clienteRequestDto, clienteModel);

        clienteModel.setSenha(passwordEncoder.encode(clienteRequestDto.senha()));

        return new ClienteResponseDto(clienteRepository.save(clienteModel));
    }

    @Transactional
    public ClienteResponseDto updateCliente(UUID id, ClienteRequestDto clienteRequestDto){
        clienteRepository.findByEmail(clienteRequestDto.email()).ifPresent(cliente -> {
            if (!cliente.getId().equals(id)) {
                throw new EntityExistsException("Email já cadastrado.");
            }
        });

        clienteRepository.findByCpf(clienteRequestDto.cpf()).ifPresent(cliente -> {
            if (!cliente.getId().equals(id)) {
                throw new EntityExistsException("CPF já cadastrado.");
            }
        });

        ClienteModel clienteModel = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);
        BeanUtils.copyProperties(clienteRequestDto, clienteModel, "senha");

        if(clienteRequestDto.senha() != null && !clienteRequestDto.senha().trim().isEmpty()){
            clienteModel.setSenha(passwordEncoder.encode(clienteRequestDto.senha()));
        }

        return new ClienteResponseDto(clienteRepository.save(clienteModel));
    }


    public ClienteResponseDto getClienteById(UUID id){
        return new ClienteResponseDto(clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new));
    }


    public List<ClienteResponseDto> getClienteByNome(String nome){
        return clienteRepository.findByNomeContainingIgnoreCase(nome).stream().map(ClienteResponseDto::new).toList();
    }


    public List<ClienteResponseDto> getAllClientes(){
        return clienteRepository.findAll().stream().map(ClienteResponseDto::new).toList();
    }


    @Transactional
    public void deleteCliente(UUID id){
        if(!clienteRepository.existsById(id)){
            throw new ClienteNotFoundException();
        }
        clienteRepository.deleteById(id);
    }

}
