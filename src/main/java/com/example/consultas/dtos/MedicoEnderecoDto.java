package com.example.consultas.dtos;

import com.example.consultas.models.MedicoEnderecoModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MedicoEnderecoDto(UUID id, @NotBlank String uf, @NotBlank String cidade, @NotBlank String cep, @NotBlank String rua, @NotBlank String numero, @NotNull UUID medico_id) {

    public MedicoEnderecoDto(MedicoEnderecoModel medicoEnderecoModel){
        this(medicoEnderecoModel.getId(), medicoEnderecoModel.getUf(), medicoEnderecoModel.getCidade(), medicoEnderecoModel.getCep(), medicoEnderecoModel.getRua(), medicoEnderecoModel.getNumero(), medicoEnderecoModel.getMedico().getId());
    }
}
