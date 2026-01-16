package com.example.consultas.dtos.consulta;

import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaResponseDto(UUID id, LocalDateTime data, String tipo, String descricao, Status status,
                                  UUID medicoId, UUID clienteId) {

    public ConsultaResponseDto(ConsultaModel consultaModel) {
        this(
                consultaModel.getId(),
                consultaModel.getDataConsulta(),
                consultaModel.getTipoConsulta(),
                consultaModel.getDescricaoConsulta(),
                consultaModel.getStatus(),
                consultaModel.getMedico().getId(),
                consultaModel.getCliente().getId()
        );
    }
}
