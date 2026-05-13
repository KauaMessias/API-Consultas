package com.example.consultas.exceptions;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException(){super("Token revogado");};
    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}
