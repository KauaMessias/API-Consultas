package com.example.consultas.dtos;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationDto(@NotBlank String email, @NotBlank String senha) {
}
