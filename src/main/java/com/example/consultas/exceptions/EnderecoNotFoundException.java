package com.example.consultas.exceptions;

public class EnderecoNotFoundException extends RuntimeException {
    public EnderecoNotFoundException(String message) {
        super(message);
    }

    public EnderecoNotFoundException() {
        super("Endereço não encontrado.");
    }
}
