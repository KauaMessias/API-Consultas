package com.example.consultas.services;

import com.example.consultas.dtos.ConsultaDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.ConflitoConsultaException;
import com.example.consultas.exceptions.ConsultaNotFoundException;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.MedicoRepository;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.checkerframework.common.value.qual.DoesNotMatchRegex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultaServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private ClienteRepository clienteRepository;


    @InjectMocks
    private ConsultaService consultaService;
    
    private LocalDateTime data = LocalDateTime.of(2030, 1, 1, 10, 0);

    @Test
    void getConsultaByMedicoId() {
        MedicoModel medico = criarMedico();

        ConsultaModel consultaModel = new ConsultaModel(null, data, "rotina", "consulta de rotina", medico, null);

    }

    @Test
    void getConsultaByClienteId() {
    }

    @Test
    @DisplayName("Deve retornar a consulta pelo id")
    void getConsultaById() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();

        UUID consulta_id = UUID.randomUUID();
        ConsultaModel consulta = new  ConsultaModel(consulta_id, data, "rotina", "consulta de rotina", medico, cliente);

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.of(consulta));

        ConsultaDto result = consultaService.getConsultaById(consulta_id);

        verify(consultaRepository, times(1)).findById(consulta_id);

        assertEquals(consulta.getId(), result.id());
        assertEquals(consulta.getDataConsulta(), result.dataConsulta());
        assertEquals(consulta.getTipoConsulta(), result.tipoConsulta());
        assertEquals(consulta.getDescricaoConsulta(), result.descricaoConsulta());
    }

    @Test
    @DisplayName("Não deve retornar consulta se ela não existir.")
    void getConsultaCase2() {
        UUID consulta_id = UUID.randomUUID();

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ConsultaNotFoundException.class, () -> consultaService.getConsultaById(consulta_id));
        assertEquals("Consulta não encontrada.", exception.getMessage());

        verify(consultaRepository, times(1)).findById(consulta_id);
    }

    @Test
    @DisplayName("Deve criar a consulta.")
    void addConsultaCase1() {
        MedicoModel medico  = new MedicoModel();
        UUID medico_id = UUID.randomUUID();
        medico.setId(medico_id);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        ClienteModel cliente = new ClienteModel();
        UUID cliente_id = UUID.randomUUID();
        cliente.setId(cliente_id);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medico_id)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(cliente_id)).thenReturn(Optional.of(cliente));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(false);


        ConsultaDto consultaDto = new ConsultaDto(null, data, "Rotina", "Consulta de rotina", medico_id, cliente_id);

        ConsultaModel saved = new ConsultaModel();
        saved.setId(UUID.randomUUID());
        saved.setDataConsulta(consultaDto.dataConsulta());
        saved.setTipoConsulta(consultaDto.tipoConsulta());
        saved.setDescricaoConsulta(consultaDto.descricaoConsulta());
        saved.setMedico(medico);
        saved.setCliente(cliente);

        when(consultaRepository.save(any())).thenReturn(saved);

        ConsultaDto result = consultaService.addConsulta(consultaDto);

        verify(consultaRepository, times(1)).save(any());

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(result.dataConsulta(), consultaDto.dataConsulta());
        assertEquals(result.tipoConsulta(), consultaDto.tipoConsulta());
        assertEquals(result.medico_id(), consultaDto.medico_id());
        assertEquals(result.cliente_id(), consultaDto.cliente_id());
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o cliente não existir.")
    void addConsultaCase2() {
        MedicoModel medico  = new MedicoModel();
        UUID medico_id = UUID.randomUUID();
        medico.setId(medico_id);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        UUID cliente_id = UUID.randomUUID();

        when(medicoRepository.findById(medico_id)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(cliente_id)).thenReturn(Optional.empty());

        ConsultaDto consultaDto = new ConsultaDto(null, data, "Rotina", "Consulta de rotina", medico_id, cliente_id);

        Exception exception = assertThrows(ClienteNotFoundException.class, () -> consultaService.addConsulta(consultaDto));
        assertEquals("Cliente não encontrado.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o medico não existir.")
    void addConsultaCase3() {
        UUID medico_id = UUID.randomUUID();

        ClienteModel cliente = new ClienteModel();
        UUID cliente_id = UUID.randomUUID();
        cliente.setId(cliente_id);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medico_id)).thenReturn(Optional.empty());

        ConsultaDto consultaDto = new ConsultaDto(null, data, "Rotina", "Consulta de rotina", medico_id, cliente_id);

        Exception exception = assertThrows(MedicoNotFoundException.class, () -> consultaService.addConsulta(consultaDto));
        assertEquals("Medico não encontrado.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o medico possuir outra no mesmo horário.")
    void addConsultaCase4(){
        MedicoModel medico  = new MedicoModel();
        UUID medico_id = UUID.randomUUID();
        medico.setId(medico_id);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        ClienteModel cliente = new ClienteModel();
        UUID cliente_id = UUID.randomUUID();
        cliente.setId(cliente_id);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medico_id)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(cliente_id)).thenReturn(Optional.of(cliente));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(true);

        ConsultaDto consultaDto = new ConsultaDto(null, data, "Rotina", "Consulta de rotina", medico_id, cliente_id);

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.addConsulta(consultaDto));

        assertEquals("Médico já possui uma consulta no horário.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o cliente possuir outra no mesmo horário.")
    void  addConsultaCase5(){
        MedicoModel medico  = new MedicoModel();
        UUID medico_id = UUID.randomUUID();
        medico.setId(medico_id);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        ClienteModel cliente = new ClienteModel();
        UUID cliente_id = UUID.randomUUID();
        cliente.setId(cliente_id);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medico_id)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(cliente_id)).thenReturn(Optional.of(cliente));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(true);

        ConsultaDto consultaDto = new ConsultaDto(null, data,  "Rotina", "Consulta de rotina", medico_id, cliente_id);

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.addConsulta(consultaDto));

        assertEquals("Cliente já possui uma consulta no horário.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Deve atualizar a consulta.")
    void updateConsultaCase1() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();

        UUID consulta_id = UUID.randomUUID();
        ConsultaModel consulta = new ConsultaModel(consulta_id, data, "Rotina", "Consulta de rotina", medico, cliente);

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(false);
        when(consultaRepository.save(any())).thenReturn(consulta);

        ConsultaDto consultaDto = new ConsultaDto(consulta_id, data.plusDays(1), "Nova Rotina", "Nova Consulta Rotina", medico.getId(), cliente.getId());

        ConsultaDto result = consultaService.updateConsulta(consulta_id, consultaDto);

        verify(consultaRepository, times(1)).save(any(ConsultaModel.class));

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(consultaDto.tipoConsulta(), result.tipoConsulta());
        assertEquals(consultaDto.dataConsulta(), result.dataConsulta());
        assertEquals(consultaDto.descricaoConsulta(), result.descricaoConsulta());
    }

    @Test
    @DisplayName("Não deve atualizar a consulta se a consulta não existe.")
    void updateConsultaCase2() {
        UUID consulta_id = UUID.randomUUID();

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.empty());

        ConsultaDto consultaDto = new ConsultaDto(null, data, "Rotina", "Consulta de rotina", null, null);

        Exception exception = assertThrows(ConsultaNotFoundException.class, () -> consultaService.updateConsulta(consulta_id, consultaDto));
        assertEquals("Consulta não encontrada.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve atualizar a consulta se o médico possuir outra no horário.")
    void updateConsultaCase3() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();

        UUID consulta_id = UUID.randomUUID();
        ConsultaModel consulta = new ConsultaModel(consulta_id, data, "Rotina", "Consulta de rotina", medico, cliente);

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(true);

        ConsultaDto consultaDto = new ConsultaDto(consulta_id, data.plusDays(1), "`Nova Rotina", "Nova Consulta de rotina", medico.getId(), cliente.getId());

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.updateConsulta(consulta_id, consultaDto));
        assertEquals("Conflito no horário da consulta.", exception.getMessage());

        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve atualizar a consulta se o cliente possuir outra no horário.")
    void updateConsultaCase4() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();

        UUID consulta_id = UUID.randomUUID();
        ConsultaModel consulta = new ConsultaModel(consulta_id, data, "Rotina", "Consulta de rotina", medico, cliente);

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(true);

        ConsultaDto consultaDto = new ConsultaDto(consulta_id, data.plusDays(1), "`Nova Rotina", "Nova Consulta de rotina", medico.getId(), cliente.getId());

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.updateConsulta(consulta_id, consultaDto));
        assertEquals("Conflito no horário da consulta.", exception.getMessage());

        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Deve deletar a consulta.")
    void deleteConsulta() {
        UUID consulta_id = UUID.randomUUID();
        ConsultaModel consulta = new ConsultaModel(consulta_id, data, "Rotina", "Consulta de rotina", null, null);

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.of(consulta));

        consultaService.deleteConsulta(consulta_id);

        verify(consultaRepository, times(1)).delete(consulta);
    }

    @Test
    @DisplayName("Não deve deletar a consulta se não encontrar nenhuma.")
    void deleteConsultaCase2(){
        UUID consulta_id = UUID.randomUUID();

        when(consultaRepository.findById(consulta_id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ConsultaNotFoundException.class, () -> consultaService.deleteConsulta(consulta_id));

        assertEquals("Consulta não encontrada.", exception.getMessage());

        verify(consultaRepository, never()).delete(any(ConsultaModel.class));
    }
    private MedicoModel criarMedico(){
        return new MedicoModel(UUID.randomUUID(), "Jorge", "4325", "(71)99999-9999", "Urologista", null, null);
    }

    private ClienteModel criarCliente(){
        return new ClienteModel(UUID.randomUUID(), "Fernando", "12345678901", "(71)99999-9999", null, null);
    }
}