package com.example.consultas.models;

import java.time.DayOfWeek;

public enum DiaSemana {
    SEGUNDA(1), TERCA(2), QUARTA(3), QUINTA(4), SEXTA(5), SABADO(6), DOMINGO(7);

    int dia;

    DiaSemana(int dia){
        this.dia = dia;
    }

    public static DiaSemana from(DayOfWeek dia){
        return switch (dia){
            case MONDAY -> SEGUNDA;
            case TUESDAY -> TERCA;
            case WEDNESDAY -> QUARTA;
            case THURSDAY -> QUINTA;
            case FRIDAY -> SEXTA;
            case SATURDAY -> SABADO;
            case SUNDAY -> DOMINGO;
        };
    }
}
