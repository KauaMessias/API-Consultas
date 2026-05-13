package com.example.consultas.models;

public enum Status {
    PENDENTE("pendente"), CONCLUIDA("concluida"), CANCELADA("cancelada");
    private String status;

    private Status(String status) {
        this.status = status;
    }
}
