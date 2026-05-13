package com.example.consultas.repositories;

import com.example.consultas.dtos.auth.AuthenticationDto;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Test
    @DisplayName("Deve retornar um usuário do bd com sucesso.")
    void findByEmailCase1() {
        AuthenticationDto authenticationDto = new AuthenticationDto("carlos@gmail.com", "1234");
        criarUsuario(authenticationDto);

        Optional<UserDetails> usuario = usuarioRepository.findByEmail("carlos@gmail.com");

        assertThat(usuario.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Não deve retornar um usuário do bd com sucesso quando ele não existir.")
    void findByEmailCase2() {
        AuthenticationDto authenticationDto = new AuthenticationDto("carlos@gmail.com", "1234");

        Optional<UserDetails> usuario = usuarioRepository.findByEmail("carlos@gmail.com");

        assertThat(usuario.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true quando encontrar um usuário com o email enviado.")
    void existsByEmail() {
        AuthenticationDto authenticationDto = new AuthenticationDto("carlos@gmail.com", "1234");
        criarUsuario(authenticationDto);

        Optional<UserDetails> usuario = usuarioRepository.findByEmail("carlos@gmail.com");

        assertThat(usuario.isPresent()).isTrue();
    }

    private UsuarioModel criarUsuario(AuthenticationDto authenticationDto) {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(authenticationDto.email());
        usuario.setSenha(authenticationDto.senha());
        usuario.setRole(Roles.CLIENTE);
        usuario.setEnabled(true);
        em.persist(usuario);
        return usuario;
    };
}