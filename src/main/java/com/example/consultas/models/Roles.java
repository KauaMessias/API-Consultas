package com.example.consultas.models;

public enum Roles {
    MEDICO("medico"), CLIENTE("cliente"), ADMIN("admin");

    private String role;

    Roles(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }
}
