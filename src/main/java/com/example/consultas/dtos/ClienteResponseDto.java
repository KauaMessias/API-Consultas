package com.example.consultas.dtos;

import com.example.consultas.models.ClienteModel;

import java.util.UUID;

public record ClienteResponseDto(UUID id, String nome, String email, String cpf, String telefone) {

    public ClienteResponseDto(ClienteModel clienteModel){
        this(clienteModel.getId(), clienteModel.getNome(), clienteModel.getEmail(), clienteModel.getCpf(), clienteModel.getTelefone());
    }
}
