package com.example.consultas.dtos.consulta;

import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaUpdateDto(LocalDateTime data, String tipo, String descricao) {

    public ConsultaModel updateConsulta(ConsultaModel consultaModel) {
        consultaModel.setDataConsulta(data);
        consultaModel.setTipoConsulta(tipo);
        consultaModel.setDescricaoConsulta(descricao);
        return consultaModel;
    }
}
