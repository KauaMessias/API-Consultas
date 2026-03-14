package com.example.consultas.dtos.medico;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.models.MedicoModel;

import java.util.UUID;

public record MedicoResponseDto(UUID id, String nome, String crm, String email, String telefone, String especialidade, EnderecoDto enderecoPrincipal) {

    public MedicoResponseDto(MedicoModel medicoModel){
        this(medicoModel.getId(), medicoModel.getNome(), medicoModel.getCrm(), medicoModel.getUsuario().getUsername(), medicoModel.getTelefone(), medicoModel.getEspecialidade(), null);
    }


    public MedicoResponseDto(MedicoModel medicoModel, EnderecoDto enderecoPrincipal){
        this(medicoModel.getId(), medicoModel.getNome(), medicoModel.getCrm(), medicoModel.getUsuario().getUsername(), medicoModel.getTelefone(), medicoModel.getEspecialidade(), enderecoPrincipal);
    }
}
