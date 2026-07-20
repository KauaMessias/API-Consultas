package com.example.consultas.exceptions;

public class UsuarioValidacaoException extends RuntimeException {

    public UsuarioValidacaoException(){super("Usuário já validado.");}

    public UsuarioValidacaoException(String message) {
        super(message);
    }
}
