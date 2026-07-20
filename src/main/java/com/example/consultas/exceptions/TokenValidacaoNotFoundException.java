package com.example.consultas.exceptions;

public class TokenValidacaoNotFoundException extends RuntimeException {
    public TokenValidacaoNotFoundException() {
        super("Token de validação não encontrado.");
    }
    public TokenValidacaoNotFoundException(String message) {
        super(message);
    }
}
