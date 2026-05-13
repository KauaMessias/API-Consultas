package com.example.consultas.exceptions;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException(){super("Token não encontrado");}
    public RefreshTokenNotFoundException(String message) {
        super(message);
    }

}
