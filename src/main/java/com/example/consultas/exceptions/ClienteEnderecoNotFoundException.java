package com.example.consultas.exceptions;

public class ClienteEnderecoNotFoundException extends RuntimeException {
    public ClienteEnderecoNotFoundException(String message) {
        super(message);
    }

    public ClienteEnderecoNotFoundException() {super("Endereço não encontrado.");}
}
