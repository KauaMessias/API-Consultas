package com.example.consultas.dtos;

import com.example.consultas.models.MedicoModel;

import java.util.UUID;

public record MedicoResponseDto(UUID id,String nome, String crm, String email, String telefone, String especialidade) {

    public MedicoResponseDto(MedicoModel medicoModel){
        this(medicoModel.getId(), medicoModel.getNome(), medicoModel.getCrm(), medicoModel.getUsuario().getUsername(), medicoModel.getTelefone(), medicoModel.getEspecialidade());
    }
}
