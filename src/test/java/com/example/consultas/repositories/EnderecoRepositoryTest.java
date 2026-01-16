package com.example.consultas.repositories;

import com.example.consultas.models.EnderecoModel;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class EnderecoRepositoryTest {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private EntityManager entityManager;


    @Test
    @DisplayName("Deve retornar true quando existir um endereco com o id inserido relacionado ao usuario com o id inserido.")
    void existsByIdAndUsuario_IdTrue() {
        UsuarioModel usuario = gerarUsuario();
        EnderecoModel endereco = gerarEndereco(usuario);

        entityManager.flush();

        boolean result = enderecoRepository.existsByIdAndUsuario_Id(endereco.getId(), usuario.getId());

        assertTrue(result);
    }

    @Test
    @DisplayName("Deve retornar false quando não existir um endereco com o id inserido relacionado ao usuario com o id inserido.")
    void existsByIdAndUsuario_IdFalse() {
        boolean result = enderecoRepository.existsByIdAndUsuario_Id(UUID.randomUUID(), UUID.randomUUID());

        assertFalse(result);
    }


    @Test
    @DisplayName("Deve remover todos os endereços relacionados a um usuário.")
    void deleteByUsuario_Id() {
        UsuarioModel usuario = gerarUsuario();
        EnderecoModel e1 = gerarEndereco(usuario);
        EnderecoModel e2 = gerarEndereco(usuario);
        entityManager.flush();

        assertTrue(enderecoRepository.existsByIdAndUsuario_Id(e1.getId(), usuario.getId()));
        assertTrue(enderecoRepository.existsByIdAndUsuario_Id(e2.getId(), usuario.getId()));

        enderecoRepository.deleteByUsuario_Id(usuario.getId());

        entityManager.flush();
        Page<EnderecoModel> result = enderecoRepository.findAllByUsuario_Id(usuario.getId(), PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Deve retornar o endereço que tenha o id inserido relacionado ao usuário com o outro id inserido.")
    void findByIdAndUsuario_IdExists() {
        UsuarioModel usuario = gerarUsuario();
        EnderecoModel endereco = gerarEndereco(usuario);
        entityManager.flush();

        Optional<EnderecoModel> result = enderecoRepository.findByIdAndUsuario_Id(endereco.getId(), usuario.getId());

        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("Não deve retornar nada se o endereço que tenha o id inserido relacionado ao usuário com o outro id inserido não existir.")
    void findByIdAndUsuario_IdEmpty() {
        Optional<EnderecoModel> result = enderecoRepository.findByIdAndUsuario_Id(UUID.randomUUID(), UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Deve retornar todos os endereços que tenham um bairro semelhante ao inserido.")
    void findByBairroContainingIgnoreCase() {
        UsuarioModel usuario = gerarUsuario();
        EnderecoModel e1 = gerarEndereco(usuario);
        EnderecoModel e2 = gerarEndereco(usuario);

        Pageable pageable = PageRequest.of(0, 10);

        Page<EnderecoModel> result = enderecoRepository.findByBairroContainingIgnoreCase(e1.getBairro(), pageable);

        assertTrue(result.hasContent());
        assertEquals(2, result.getTotalElements());
        assertEquals(e1.getBairro(), result.getContent().getFirst().getBairro());
        assertEquals(e1.getBairro(), result.getContent().get(1).getBairro());
    }

    @Test
    @DisplayName("Deve retornar todos os endereços que tenham uma cidade semelhante ao inserido.")
    void findByCidadeContainingIgnoreCase() {
        UsuarioModel usuario = gerarUsuario();
        EnderecoModel e1 = gerarEndereco(usuario);
        EnderecoModel e2 = gerarEndereco(usuario);
        Pageable pageable = PageRequest.of(0, 10);

        Page<EnderecoModel> result = enderecoRepository.findByCidadeContainingIgnoreCase(e1.getCidade(), pageable);

        assertTrue(result.hasContent());
        assertEquals(2, result.getTotalElements());
        assertEquals(e1.getCidade(), result.getContent().getFirst().getCidade());
        assertEquals(e2.getCidade(), result.getContent().getFirst().getCidade());
    }


    @Test
    @DisplayName("Deve retornar um page com todos os endereços associados ao usuário com o id inserido.")
    void findAllByUsuario_Id() {
        UsuarioModel usuario = gerarUsuario();
        EnderecoModel e1 = gerarEndereco(usuario);
        EnderecoModel e2 = gerarEndereco(usuario);
        Pageable pageable = PageRequest.of(0, 10);

        Page<EnderecoModel> result = enderecoRepository.findAllByUsuario_Id(usuario.getId(), pageable);

        assertTrue(result.hasContent());
        assertEquals(2, result.getTotalElements());
        assertEquals(e1.getUsuario().getId(), result.getContent().getFirst().getUsuario().getId());
        assertEquals(e1.getUsuario().getId(), result.getContent().get(1).getUsuario().getId());

    }


    private EnderecoModel gerarEndereco(UsuarioModel usuario) {
        EnderecoModel endereco = new EnderecoModel();
        endereco.setUsuario(gerarUsuario());
        endereco.setUf("BA");
        endereco.setCep("12345");
        endereco.setRua("Rua Teixeira");
        endereco.setBairro("Brotas");
        endereco.setCidade("Salvador");
        endereco.setNumero("53");
        endereco.setUsuario(usuario);
        entityManager.persist(endereco);

        return endereco;
    }

    private UsuarioModel gerarUsuario() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(UUID.randomUUID()+"@gmail.com");
        usuario.setSenha("123456");
        usuario.setRole(Roles.MEDICO);
        usuario.setEnabled(true);
        entityManager.persist(usuario);

        return usuario;
    }
}