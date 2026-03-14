package com.example.consultas.dtos.medico;

import com.example.consultas.models.DiaSemana;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record HorarioDisponivelDto(LocalDate data, LocalTime horario, UUID horarioBase) {
}
