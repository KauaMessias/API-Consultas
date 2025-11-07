package com.example.consultas.dtos;

import com.example.consultas.models.MedicoModel;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record MedicoResponseDto(UUID id,String nome, String crm, String email, String senha, String telefone) {

    public MedicoResponseDto(MedicoModel medicoModel){
        this(medicoModel.getId(), medicoModel.getNome(), medicoModel.getCrm(), medicoModel.getEmail(), medicoModel.getSenha(), medicoModel.getTelefone());
    }
}
