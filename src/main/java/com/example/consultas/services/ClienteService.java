package com.example.consultas.services;

import com.example.consultas.dtos.ClienteRequestDto;
import com.example.consultas.dtos.ClienteResponseDto;
import com.example.consultas.dtos.MedicoResponseDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository){
        this.clienteRepository = clienteRepository;
    }


    public ClienteResponseDto addCliente(ClienteRequestDto clienteRequestDto){
        ClienteModel clienteModel = new ClienteModel();
        BeanUtils.copyProperties(clienteRequestDto, clienteModel);
        clienteModel = clienteRepository.save(clienteModel);

        return new ClienteResponseDto(clienteModel);
    }


    public ClienteResponseDto updateCliente(UUID id, ClienteRequestDto clienteRequestDto){
        ClienteModel clienteModel = clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new);
        BeanUtils.copyProperties(clienteRequestDto, clienteModel);
        clienteModel = clienteRepository.save(clienteModel);

        return new ClienteResponseDto(clienteModel);
    }


    public ClienteResponseDto getClienteById(UUID id){
        return new ClienteResponseDto(clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new));
    }


    public ClienteResponseDto getClienteByCpf(String cpf){
        return new ClienteResponseDto(clienteRepository.findByCpf(cpf).orElseThrow(ClienteNotFoundException::new));
    }


    public ClienteResponseDto getClienteByEmail(String email){
        return new ClienteResponseDto(clienteRepository.findByEmail(email).orElseThrow(ClienteNotFoundException::new));
    }


    public List<ClienteResponseDto> getClienteByNome(String nome){
        return clienteRepository.findByNomeContainingIgnoreCase(nome).stream().map(ClienteResponseDto::new).toList();
    }


    public List<ClienteResponseDto> getAllClientes(){
        return clienteRepository.findAll().stream().map(ClienteResponseDto::new).toList();
    }


    @Transactional
    public void deleteCliente(UUID id){
        clienteRepository.delete(clienteRepository.findById(id).orElseThrow(ClienteNotFoundException::new));
    }

}
