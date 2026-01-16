package com.example.consultas.repositories;

import com.example.consultas.dtos.AuthenticationDto;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import jakarta.persistence.EntityManager;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MedicoRepositoryTest {

    @Autowired
    MedicoRepository medicoRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("Deve retornar o médico que possui o crm inserido.")
    void findByCrmCase1() {
        MedicoModel medico = criarMedico();
        em.flush();

        Optional<MedicoModel> result = medicoRepository.findByCrm(medico.getCrm());

        assertTrue(result.isPresent());
        assertEquals(medico.getCrm(), result.get().getCrm());
    }

    @Test
    @DisplayName("Não deve retornar o médico se o crm for inexistente.")
    void findByCrmCase2() {
        Optional<MedicoModel> result = medicoRepository.findByCrm("4123");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar um Page com os usuários que contém um nome semelhante ao inserido.")
    void findByNomeContainingIgnoreCase() {
        MedicoModel medico = criarMedico();
        Pageable pageable = PageRequest.of(0, 10);
        em.flush();

        Page<MedicoModel> medicos = medicoRepository.findByNomeContainingIgnoreCase(pageable, medico.getNome());

        assertFalse(medicos.isEmpty());
        assertEquals(medico.getNome(), medicos.getContent().get(0).getNome());
        assertEquals(1, medicos.getTotalElements());
    }

    @Test
    @DisplayName("Deve retornar true quando existir um medico com o id e um usuário relacionado com o outro id.")
    void existsByIdAndUsuario_IdCase1() {
        MedicoModel medico = criarMedico();
        em.flush();

        assertTrue(medicoRepository.existsByIdAndUsuario_Id(medico.getId(), medico.getUsuario().getId()));
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um medico com o id e um usuário relacionado com o outro id.")
    void existsByIdAndUsuario_IdCase2() {
        assertFalse(medicoRepository.existsByIdAndUsuario_Id(UUID.randomUUID(), UUID.randomUUID()));
    }

    @Test
    @DisplayName("Deve retornar true quando existir um medico com o crm.")
    void existsByCrmCase1() {
        MedicoModel medico = criarMedico();
        em.flush();

        assertTrue(medicoRepository.existsByCrm(medico.getCrm()));
    }

    @Test
    @DisplayName("Deve retornar false quando não")
    void existsByCrmCase2(){
        assertFalse(medicoRepository.existsByCrm("123"));
    }

    @Test
    @DisplayName("Deve retornar true quando existir um médico quem tenha relação com o usuário do id selecionado.")
    void existsByUsuario_IdCase1() {
        MedicoModel medico = criarMedico();
        em.flush();

        assertTrue(medicoRepository.existsByUsuario_Id(medico.getUsuario().getId()));
    }

    @Test
    @DisplayName("Deve retornar true quando existir não existir um médico quem tenha relação com o usuário do id selecionado.")
    void existsByUsuario_IdCase2() {
        assertFalse(medicoRepository.existsByUsuario_Id(UUID.randomUUID()));
    }

    private MedicoModel criarMedico(){
        MedicoModel medico = new MedicoModel();
        medico.setNome("Pedro");
        medico.setCrm("1234567");
        medico.setEspecialidade("Nutricionista");
        medico.setTelefone("(71)99999-9999");
        medico.setUsuario(criarUsuario());
        em.persist(medico);

        return medico;
    }

    private UsuarioModel criarUsuario() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail("carlos@gmail.com");
        usuario.setSenha("1234567");
        usuario.setRole(Roles.CLIENTE);
        usuario.setEnabled(true);
        em.persist(usuario);

        return usuario;
    };
}