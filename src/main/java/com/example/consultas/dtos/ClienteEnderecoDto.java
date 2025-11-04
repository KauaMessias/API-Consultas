package com.example.consultas.dtos;

import com.example.consultas.models.ClienteEnderecoModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ClienteEnderecoDto(UUID id, @NotBlank String uf, @NotBlank String cidade, @NotBlank String cep, @NotBlank String rua, @NotBlank String numero, @NotNull UUID cliente_id) {
    public ClienteEnderecoDto(ClienteEnderecoModel clienteEnderecoModel){
        this(clienteEnderecoModel.getId(), clienteEnderecoModel.getUf(), clienteEnderecoModel.getCidade(), clienteEnderecoModel.getCep(), clienteEnderecoModel.getRua(), clienteEnderecoModel.getNumero(), clienteEnderecoModel.getCliente().getId());
    }
}
