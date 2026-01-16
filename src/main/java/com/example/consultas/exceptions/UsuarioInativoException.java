package com.example.consultas.exceptions;

public class UsuarioInativoException extends RuntimeException {
    public UsuarioInativoException(String message) {
        super(message);
    }

    public UsuarioInativoException() {
        super("Usuário inativo");
    }
}
