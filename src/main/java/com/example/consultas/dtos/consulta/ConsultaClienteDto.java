package com.example.consultas.dtos.consulta;

import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaClienteDto(UUID id, String tipo, String descricao, LocalDateTime data, Status status, UUID medicoId, String nomeMedico, String crm, String especialidade) {

    public ConsultaClienteDto(ConsultaModel consulta){
        this(consulta.getId(), consulta.getTipoConsulta(), consulta.getDescricaoConsulta(), consulta.getDataConsulta(), consulta.getStatus(), consulta.getMedico().getId(), consulta.getMedico().getNome(), consulta.getMedico().getCrm(), consulta.getMedico().getEspecialidade());
    }
}
