package com.example.consultas.security;

import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.MedicoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authz")
public class UserAuthorization {

    private final MedicoRepository medicoRepository;
    private final ClienteRepository clienteRepository;

    public UserAuthorization(MedicoRepository medicoRepository, ClienteRepository clienteRepository) {
        this.medicoRepository = medicoRepository;
        this.clienteRepository = clienteRepository;
    }

    public boolean autorizado(UUID id, Authentication authentication) {
        if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }
        UsuarioModel usuario = (UsuarioModel) authentication.getPrincipal();

        if(medicoRepository.existsByIdAndUsuario_Id(id, usuario.getId())) {
            return true;
        }

        return clienteRepository.existsByIdAndUsuario_Id(id, usuario.getId());
    }

}
