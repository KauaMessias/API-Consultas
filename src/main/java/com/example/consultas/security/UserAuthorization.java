package com.example.consultas.security;

import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.EnderecoRepository;
import com.example.consultas.repositories.MedicoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("authz")
public class UserAuthorization {

    private final MedicoRepository medicoRepository;
    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ConsultaRepository consultaRepository;

    public UserAuthorization(MedicoRepository medicoRepository, ClienteRepository clienteRepository, EnderecoRepository enderecoRepository, ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.consultaRepository = consultaRepository;
    }

    public boolean acessoCliente(UUID id, Authentication authentication) {
        UsuarioModel usuario = (UsuarioModel) authentication.getPrincipal();

        if (hasRole(usuario, Roles.ADMIN)) {
            return true;
        }

        if (hasRole(usuario, Roles.CLIENTE)) {
            return clienteRepository.existsByIdAndUsuario_Id(id, usuario.getId());
        }

        if (hasRole(usuario, Roles.MEDICO)) {
            return consultaRepository.existsByMedico_Usuario_IdAndCliente_Id(usuario.getId(), id);
        }
        return false;
    }

    public boolean acessoMedico(UUID medico_id, Authentication authentication) {
        UsuarioModel usuario = (UsuarioModel) authentication.getPrincipal();

        if (hasRole(usuario, Roles.ADMIN)) {
            return true;
        }

        return medicoRepository.existsByIdAndUsuario_Id(medico_id, usuario.getId());
    }

    public boolean acessoConsulta(UUID id, Authentication authentication) {
        UsuarioModel usuario = (UsuarioModel) authentication.getPrincipal();

        if (hasRole(usuario, Roles.ADMIN)) {
            return true;
        }

        if (hasRole(usuario, Roles.CLIENTE)) {
            return consultaRepository.existsByIdAndCliente_Usuario_Id(id, usuario.getId());
        }

        if (hasRole(usuario, Roles.MEDICO)) {
            return consultaRepository.existsByIdAndMedico_Usuario_Id(id, usuario.getId());
        }

        return false;
    }

    public boolean acessoEndereco(UUID endereco_id, Authentication authentication) {
        UsuarioModel usuario = (UsuarioModel) authentication.getPrincipal();
        if (hasRole(usuario, Roles.ADMIN)) {
            return true;
        }
        return enderecoRepository.existsByIdAndUsuario_Id(endereco_id, usuario.getId());
        }


    public boolean hasRole(UsuarioModel usuario, Roles role) {
        return usuario.getRole().equals(role);
    }

}
