package com.example.consultas.exceptions;

public class TokenValidacaoExpiradoException extends RuntimeException {
    public TokenValidacaoExpiradoException() {
        super("Token de validação expirado.");
    }
    public TokenValidacaoExpiradoException(String message) {
        super(message);
    }
}
