package com.example.consultas.exceptions;

public class HorarioNotFoundException extends RuntimeException {
    public HorarioNotFoundException(String message) {
        super(message);
    }
    public HorarioNotFoundException(){ super("Horario não encontrado.");}
}
