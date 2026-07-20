package com.example.consultas.dtos.auth;

import jakarta.validation.constraints.Pattern;

public record SenhaDto(String senhaAtual, @Pattern( regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!*?]{8,}$", message = "A senha deve conter no minimo 8 digitos, contendo pelo menos um caracter maiusculo, um minusculo, um digito e um caractere especial") String senha) {
}
