package com.example.consultas.dtos.medico;

import com.example.consultas.models.DiaSemana;
import com.example.consultas.models.HorarioMedico;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record HorarioDto(UUID id, DiaSemana diaSemana, LocalTime horarioInicio, LocalTime horarioFinal,
                         int duracao, boolean ativo) {

    public HorarioDto(HorarioMedico horarioMedico) {
        this(horarioMedico.getId(), horarioMedico.getDiaSemana(), horarioMedico.getHorarioInicio(), horarioMedico.getHorarioFinal(), horarioMedico.getDuracao(), horarioMedico.isAtivo());
    }

    public HorarioMedico toEntity() {
        return new HorarioMedico(null, diaSemana, horarioInicio, horarioFinal, duracao, ativo, null);
    }

}


