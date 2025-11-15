package com.example.consultas.security;

import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.EnderecoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("enderecoAuthz")
public class EnderecoAuthorization {

    private final EnderecoRepository enderecoRepository;

    public EnderecoAuthorization(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    public boolean autorizado(UUID endereco_id, Authentication authentication) {
        if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }
        UsuarioModel usuario = (UsuarioModel) authentication.getPrincipal();

        return enderecoRepository.existsByIdAndUsuario_Id(endereco_id, usuario.getId());
    }

}
