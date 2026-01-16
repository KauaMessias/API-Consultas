package com.example.consultas.repositories;

import com.example.consultas.models.ClienteModel;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EntityManager em;


    @Test
    @DisplayName("Deve retornar o cliente dono do cpf.")
    void findByCpfCase1() {
        ClienteModel cliente = gerarCliente();
        em.flush();

        Optional<ClienteModel> result = clienteRepository.findByCpf(cliente.getCpf());

        assertThat(result).isPresent();
        assertThat(result.get()).usingRecursiveComparison().isEqualTo(cliente);
    }

    @Test
    @DisplayName("Deve retornar nenhum cliente para um cpf inexistente.")
    void findByCpfCase2() {
        Optional<ClienteModel> result = clienteRepository.findByCpf("123456");

        assertThat(result).isEmpty();
    }


    @Test
    @DisplayName("Deve retornar um Page com os clientes que tenham um nome semelhante ao inserido.")
    void findByNomeContainingIgnoreCase() {
        ClienteModel cliente = gerarCliente();
        em.flush();

        Pageable pageable = PageRequest.of(0, 10);

        Page<ClienteModel> result = clienteRepository.findByNomeContainingIgnoreCase(pageable, cliente.getNome());

        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst()).usingRecursiveComparison().isEqualTo(cliente);
    }


    @Test
    @DisplayName("Deve retornar true ao encontrar um cliente que tenha o id inserido e esteja relacionado com o usuário com o id")
    void existsByIdAndUsuario_IdTrue() {
        ClienteModel cliente = gerarCliente();
        em.flush();

        boolean result = clienteRepository.existsByIdAndUsuario_Id(cliente.getId(), cliente.getUsuario().getId());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false ao não encontrar um cliente que tenha o cpf inserido e esteja relacionado com o usuário com o id")
    void existsByIdAndUsuario_IdFalse() {
        boolean result = clienteRepository.existsByIdAndUsuario_Id(UUID.randomUUID(), UUID.randomUUID());

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Deve retornar true ao encontrar um cliente que tenha o cpf informado.")
    void existsByCpfFound() {
        ClienteModel cliente = gerarCliente();
        em.flush();

        boolean result = clienteRepository.existsByCpf(cliente.getCpf());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false ao não encontrar um cliente que tenha o cpf informado.")
    void existsByCpfNotFound() {
        boolean result = clienteRepository.existsByCpf("123456");

        assertThat(result).isFalse();
    }

    private ClienteModel gerarCliente(){
        ClienteModel cliente = new ClienteModel();
        cliente.setCpf("4123");
        cliente.setNome("Carlos");
        cliente.setTelefone("(71)99999-9999");
        cliente.setUsuario(gerarUsuario());
        em.persist(cliente);

        return cliente;
    }

    private UsuarioModel gerarUsuario(){
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail("carlos@gmail.com");
        usuario.setSenha("123456");
        usuario.setRole(Roles.CLIENTE);
        usuario.setEnabled(true);
        em.persist(usuario);

        return usuario;
    }
}