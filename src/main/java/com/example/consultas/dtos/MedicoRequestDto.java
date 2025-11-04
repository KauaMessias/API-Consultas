package com.example.consultas.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MedicoRequestDto(@NotBlank String nome, @NotBlank @Pattern(regexp = "\\d+") String crm,
                               @NotBlank @Email String email,
                               @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!*?]{8,}$", message = "A senha deve conter no minimo 8 digitos, contendo pelo menos um caracter maiusculo, um minusculo, um digito e um caractere especial") String senha,
                               @NotBlank @Pattern(regexp = "^\\(\\d{2}\\)(\\d{4,5})-\\d{4}$") String telefone) {
}
