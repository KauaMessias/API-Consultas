package com.example.consultas.dtos;

import com.example.consultas.models.EnderecoModel;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record EnderecoDto(UUID id, @NotBlank String uf, @NotBlank String cidade, @NotBlank String cep, @NotBlank String bairro,
                          @NotBlank String rua, @NotBlank String numero) {

    public EnderecoDto(EnderecoModel endereco) {
        this(endereco.getId(), endereco.getUf(), endereco.getCidade(), endereco.getCep(),  endereco.getBairro(), endereco.getRua(), endereco.getNumero());
    }
}
