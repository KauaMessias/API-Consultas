package com.example.consultas.models;

public enum StatusValidacao {
    PENDENTE("pendente"), USADO("usado"), EXPIRADO("expirado"), CANCELADO("cancelado");

    private String status;

    StatusValidacao(String status) {
        this.status = status;
    }
}
