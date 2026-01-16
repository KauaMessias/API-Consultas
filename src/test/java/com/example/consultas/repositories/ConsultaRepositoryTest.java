package com.example.consultas.repositories;

import com.example.consultas.models.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
class ConsultaRepositoryTest {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private EntityManager em;

    private final LocalDateTime data = LocalDateTime.of(2030, 1, 1, 10, 0);
    private final LocalDateTime consultaStart = data.minusMinutes(15);
    private final LocalDateTime consultaEnd = data.plusMinutes(14);

    @Test
    @DisplayName("Deve retornar as consultas que tenham o cliente com o id inserido.")
    void findByCliente_Id() {
        ConsultaModel consulta = gerarConsulta();
        ClienteModel cliente = consulta.getCliente();

        em.flush();

        Page<ConsultaModel> result = consultaRepository.findByCliente_Id(cliente.getId(), PageRequest.of(0, 10));

        assertThat(result.hasContent()).isTrue();
        assertThat(result).first().usingRecursiveComparison().isEqualTo(consulta);
    }

    @Test
    @DisplayName("Deve retornar as consultas que tenham o médico com o id inserido.")
    void findByMedico_Id() {
        ConsultaModel consulta = gerarConsulta();
        MedicoModel medico = consulta.getMedico();

        em.flush();

        Page<ConsultaModel> result = consultaRepository.findByMedico_Id(medico.getId(), PageRequest.of(0, 10));

        assertThat(result.hasContent()).isTrue();
        assertThat(result).first().usingRecursiveComparison().isEqualTo(consulta);
    }

    @Test
    @DisplayName("Deve retornar true quando existir uma consulta com um cliente especifico no intervalo de tempo inserido.")
    void existsByCliente_IdAndDataConsultaBetweenTrue() {
        ConsultaModel consulta = gerarConsulta();
        ClienteModel cliente = consulta.getCliente();
        em.flush();

        boolean result = consultaRepository.existsByCliente_IdAndDataConsultaBetween(cliente.getId(), consultaStart, consultaEnd);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir uma consulta com um cliente especifico no intervalo de tempo inserido.")
    void existsByCliente_IdAndDataConsultaBetweenFalse() {
        ClienteModel cliente = gerarCliente();
        em.flush();

        boolean result = consultaRepository.existsByCliente_IdAndDataConsultaBetween(cliente.getId(), consultaStart, consultaEnd);

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Deve retornar true quando existir uma consulta com um médico especifico no intervalo de tempo inserido.")
    void existsByMedico_IdAndDataConsultaBetweenTrue() {
        ConsultaModel consulta = gerarConsulta();
        MedicoModel medico = consulta.getMedico();
        em.flush();

        boolean result = consultaRepository.existsByMedico_IdAndDataConsultaBetween(medico.getId(), consultaStart, consultaEnd);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando não existir uma consulta com um médico especifico no intervalo de tempo inserido.")
    void existsByMedico_IdAndDataConsultaBetweenFalse() {
        MedicoModel medico = gerarMedico();
        em.flush();
        boolean result = consultaRepository.existsByMedico_IdAndDataConsultaBetween(medico.getId(), consultaStart, consultaEnd);

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Deve retornar true quando um cliente tiver uma outra consulta em um intervalo de tempo especifico especifico")
    void existsByCliente_IdAndDataConsultaBetweenAndIdNotTrue() {
        ConsultaModel consulta = gerarConsulta();
        ClienteModel cliente = consulta.getCliente();
        em.flush();

        boolean result = consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(cliente.getId(), consultaStart, consultaEnd, UUID.randomUUID());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando um cliente não tiver uma outra consulta em um intervalo de tempo especifico")
    void existsByCliente_IdAndDataConsultaBetweenAndIdNotFalse() {
        ClienteModel cliente = gerarCliente();
        em.flush();

        boolean result = consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(cliente.getId(), consultaStart, consultaEnd, UUID.randomUUID());

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Deve retornar true quando um médico tiver uma consulta em um intervalo de tempo especifico.")
    void existsByMedico_IdAndDataConsultaBetweenAndIdNot_True() {
            ConsultaModel consulta = gerarConsulta();
            MedicoModel medico = consulta.getMedico();
            em.flush();

            boolean result = consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(medico.getId(), consultaStart, consultaEnd, UUID.randomUUID());

            assertThat(result).isTrue();
        }

    @Test
    @DisplayName("Deve retornar false quando um médico não tiver uma consulta em um intervalo de tempo especifico.")
    void existsByMedico_IdAndDataConsultaBetweenAndIdNot_False() {
        MedicoModel medico = gerarMedico();
        em.flush();

        boolean result = consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(medico.getId(), consultaStart, consultaEnd, UUID.randomUUID());

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Deve retornar true se existir uma consulta para um médico relacionado a um usuario e para um cliente especifico.")
    void existsByMedico_Usuario_IdAndCliente_IdTrue() {
        ConsultaModel consulta = gerarConsulta();
        MedicoModel medico = consulta.getMedico();
        ClienteModel cliente = consulta.getCliente();

        em.flush();

        boolean result = consultaRepository.existsByMedico_Usuario_IdAndCliente_Id(medico.getUsuario().getId(), cliente.getId());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false se não existir uma consulta para um médico relacionado a um usuario e para um cliente especifico.")
    void existsByMedico_Usuario_IdAndCliente_IdFalse() {
        MedicoModel medico = gerarMedico();
        ClienteModel cliente = gerarCliente();

        em.flush();

        boolean result = consultaRepository.existsByMedico_Usuario_IdAndCliente_Id(medico.getUsuario().getId(), cliente.getId());

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Deve retornar true se existir uma consulta com o cliente relacionado ao usuário que tenha o id inserido.")
    void existsByCliente_Usuario_IdTrue() {
        ConsultaModel consulta = gerarConsulta();
        ClienteModel cliente = consulta.getCliente();

        em.flush();
        boolean result = consultaRepository.existsByCliente_Usuario_Id(cliente.getUsuario().getId());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false se não existir uma consulta com o cliente relacionado ao usuário que tenha o id inserido.")
    void existsByCliente_Usuario_IdFalse() {
        ClienteModel cliente = gerarCliente();
        em.flush();

        boolean result = consultaRepository.existsByCliente_Usuario_Id(cliente.getUsuario().getId());

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve retornar true se existir uma consulta com o médico relacionado ao usuário que tenha o id inserido.")
    void existsByIdAndMedico_Usuario_IdTrue() {
        ConsultaModel consulta = gerarConsulta();
        MedicoModel medico = consulta.getMedico();

        em.flush();

        boolean result = consultaRepository.existsByIdAndMedico_Usuario_Id(consulta.getId(), medico.getUsuario().getId());

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false se não existir uma consulta com o médico relacionado ao usuário que tenha o id inserido.")
    void existsByIdAndMedico_Usuario_IdFalse() {
        MedicoModel medico = gerarMedico();
        em.flush();

        boolean result = consultaRepository.existsByIdAndMedico_Usuario_Id(UUID.randomUUID(), medico.getUsuario().getId());

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Deve retornar true se existir uma consulta com o id inserido e um cliente relacionado a um usuário específico")
    void existsByIdAndCliente_Usuario_IdTrue() {
        ConsultaModel consulta = gerarConsulta();
        ClienteModel cliente = consulta.getCliente();

        em.flush();

        boolean result = consultaRepository.existsByIdAndCliente_Usuario_Id(consulta.getId(), cliente.getUsuario().getId());
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false se não existir uma consulta com o id inserido e um cliente relacionado a um usuário específico")
    void existsByIdAndCliente_Usuario_IdFalse() {
        ClienteModel cliente = gerarCliente();

        em.flush();

        boolean result = consultaRepository.existsByIdAndCliente_Usuario_Id(UUID.randomUUID(), cliente.getUsuario().getId());
        assertThat(result).isFalse();
    }

    private MedicoModel gerarMedico() {
        MedicoModel medico = new MedicoModel();
        medico.setNome("Pedro");
        medico.setCrm("1234567");
        medico.setEspecialidade("Nutricionista");
        medico.setTelefone("(71)99999-9999");
        medico.setUsuario(criarUsuarioMedico());
        em.persist(medico);

        return medico;
    }

    private ClienteModel gerarCliente() {
        ClienteModel cliente = new ClienteModel();
        cliente.setCpf("4123");
        cliente.setNome("Carlos");
        cliente.setTelefone("(71)99999-9999");
        cliente.setUsuario(gerarUsuarioCliente());
        em.persist(cliente);

        return cliente;
    }

    private UsuarioModel gerarUsuarioCliente() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(UUID.randomUUID()+"@gmail.com");
        usuario.setSenha("123456");
        usuario.setRole(Roles.CLIENTE);
        usuario.setEnabled(true);
        em.persist(usuario);

        return usuario;
    }

    private UsuarioModel criarUsuarioMedico() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmail(UUID.randomUUID()+"@gmail.com");
        usuario.setSenha("1234567");
        usuario.setRole(Roles.MEDICO);
        usuario.setEnabled(true);
        em.persist(usuario);

        return usuario;
    }

    private ConsultaModel gerarConsulta() {
        ConsultaModel consulta = new ConsultaModel();
        consulta.setTipoConsulta("Rotina");
        consulta.setDescricaoConsulta("Consulta de Rotina.");
        consulta.setDataConsulta(data);
        consulta.setStatus(Status.PENDENTE);
        consulta.setMedico(gerarMedico());
        consulta.setCliente(gerarCliente());
        em.persist(consulta);

        return consulta;
    }
}