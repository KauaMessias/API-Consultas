package com.example.consultas.models;

public enum Status {
    CONCLUIDA("concluida"), PENDENTE("pendente"), CANCELADA("cancelada");
    private String status;

    private Status(String status) {
        this.status = status;
    }
}
