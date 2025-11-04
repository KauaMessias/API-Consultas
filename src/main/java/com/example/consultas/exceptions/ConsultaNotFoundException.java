package com.example.consultas.exceptions;

public class ConsultaNotFoundException extends RuntimeException {
    public ConsultaNotFoundException(String message) {
        super(message);
    }

    public ConsultaNotFoundException(){super("Consulta não encontrada.");};
}
