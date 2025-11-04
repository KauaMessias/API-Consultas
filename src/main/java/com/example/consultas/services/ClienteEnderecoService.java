package com.example.consultas.services;

import com.example.consultas.dtos.ClienteEnderecoDto;
import com.example.consultas.exceptions.ClienteEnderecoNotFoundException;
import com.example.consultas.models.ClienteEnderecoModel;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.repositories.ClienteEnderecoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteEnderecoService {

    private final ClienteEnderecoRepository clienteEnderecoRepository;

    public ClienteEnderecoService(ClienteEnderecoRepository clienteEnderecoRepository) {
        this.clienteEnderecoRepository = clienteEnderecoRepository;
    }


    public ClienteEnderecoDto getClienteEnderecoById(UUID id) {
        return new ClienteEnderecoDto(clienteEnderecoRepository.findById(id).orElseThrow(ClienteEnderecoNotFoundException::new));
    }


    public List<ClienteEnderecoDto> getClienteEnderecoByCpf(String clienteCpf) {
        return clienteEnderecoRepository.findByCliente_Cpf(clienteCpf)
                .stream()
                .map(ClienteEnderecoDto::new)
                .toList();
    }


    public List<ClienteEnderecoDto> getClienteEnderecoByClienteID(UUID clienteId){
        return clienteEnderecoRepository.findByCliente_Id(clienteId)
                .stream()
                .map(ClienteEnderecoDto::new)
                .toList();
    }


    public List<ClienteEnderecoDto> getClienteEnderecoByCidade(String cidade) {
        return clienteEnderecoRepository.findByCidadeContainingIgnoreCase(cidade)
                .stream()
                .map(ClienteEnderecoDto::new)
                .toList();
    }

    public List<ClienteEnderecoDto> getClienteEnderecoByNome(String nome) {
        return clienteEnderecoRepository.findByCliente_NomeContainingIgnoreCase(nome)
                .stream()
                .map(ClienteEnderecoDto::new)
                .toList();
    }

    @Transactional
    public ClienteEnderecoDto addClienteEndereco(ClienteEnderecoDto clienteEnderecoDto) {
        ClienteEnderecoModel clienteEnderecoModel = new ClienteEnderecoModel();
        BeanUtils.copyProperties(clienteEnderecoDto, clienteEnderecoModel);

        ClienteModel clienteModel = new ClienteModel();
        clienteModel.setId(clienteEnderecoDto.cliente_id());
        clienteEnderecoModel.setCliente(clienteModel);

        return new ClienteEnderecoDto(clienteEnderecoRepository.save(clienteEnderecoModel));
    }

    @Transactional
    public ClienteEnderecoDto updateClienteEndereco(UUID id, ClienteEnderecoDto clienteEnderecoDto) {
        ClienteEnderecoModel clienteEnderecoModel = clienteEnderecoRepository.findById(id).orElseThrow(ClienteEnderecoNotFoundException::new);
        BeanUtils.copyProperties(clienteEnderecoDto, clienteEnderecoModel, "id", "cliente");

        ClienteModel clienteModel = new ClienteModel();
        clienteModel.setId(clienteEnderecoDto.cliente_id());
        clienteEnderecoModel.setCliente(clienteModel);

        return new ClienteEnderecoDto(clienteEnderecoRepository.save(clienteEnderecoModel));
    }


    @Transactional
    public void deleteClienteEndereco(UUID id) {
        clienteEnderecoRepository.delete(clienteEnderecoRepository.findById(id).orElseThrow(ClienteEnderecoNotFoundException::new));
    }

}
