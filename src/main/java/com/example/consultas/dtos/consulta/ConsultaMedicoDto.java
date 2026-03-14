package com.example.consultas.dtos.consulta;

import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.Status;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaMedicoDto(UUID id, String tipo, String descricao, LocalDateTime data, Status status, UUID clienteId, String nomeCliente, String cpf) {

    public ConsultaMedicoDto(ConsultaModel consulta){
        this(consulta.getId(), consulta.getTipoConsulta(), consulta.getDescricaoConsulta(), consulta.getDataConsulta(), consulta.getStatus(), consulta.getCliente().getId(), consulta.getCliente().getNome(), consulta.getCliente().getCpf());
    }
}
