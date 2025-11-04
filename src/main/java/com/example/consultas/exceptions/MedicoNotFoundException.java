package com.example.consultas.exceptions;

public class MedicoNotFoundException extends RuntimeException {

    public MedicoNotFoundException(){super("Medico não encontrado.");}
    public MedicoNotFoundException(String message) {
        super(message);
    }
}
