package com.example.consultas.exceptions;

public class ClienteNotFoundException extends RuntimeException {
    public ClienteNotFoundException(String message) {
        super(message);
    }

    public ClienteNotFoundException(){ super("Cliente não encontrado.");}
}
