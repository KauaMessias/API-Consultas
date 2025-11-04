package com.example.consultas.exceptions;

public class MedicoEnderecoNotFoundException extends RuntimeException {
    public MedicoEnderecoNotFoundException(String message) {
        super(message);
    }

    public MedicoEnderecoNotFoundException() { super("Endereço não encontrado.");}
}
