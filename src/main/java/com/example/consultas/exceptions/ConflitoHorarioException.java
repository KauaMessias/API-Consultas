package com.example.consultas.exceptions;

public class ConflitoHorarioException extends RuntimeException {
    public ConflitoHorarioException(String message) {
        super(message);
    }
    public ConflitoHorarioException(){super("Conflito no horario.");}
}
