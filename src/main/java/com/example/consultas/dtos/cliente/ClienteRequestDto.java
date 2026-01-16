package com.example.consultas.dtos.cliente;

import com.example.consultas.models.ClienteModel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClienteRequestDto(@NotBlank String nome, @NotBlank @Email String email,
                                @NotBlank @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!])[A-Za-z\\d@#$%^&+=!*?]{8,}$", message = "A senha deve conter no minimo 8 digitos, contendo pelo menos um caracter maiusculo, um minusculo, um digito e um caractere especial") String senha,
                                @NotBlank @Pattern(regexp = "\\d{11}") String cpf, @NotBlank @Pattern(regexp = "^\\(\\d{2}\\)(\\d{4,5})-\\d{4}$") String telefone) {

    public ClienteModel toEntity(){
        return new ClienteModel(null, nome, cpf, telefone, null, null);
    }

    public ClienteModel updateEntity(ClienteModel cliente){
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        return cliente;
    }

}
