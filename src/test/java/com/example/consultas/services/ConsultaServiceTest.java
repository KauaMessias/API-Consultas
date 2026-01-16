package com.example.consultas.services;

import com.example.consultas.dtos.consulta.ConsultaDto;
import com.example.consultas.dtos.consulta.ConsultaResponseDto;
import com.example.consultas.dtos.consulta.ConsultaUpdateDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.ConflitoConsultaException;
import com.example.consultas.exceptions.ConsultaNotFoundException;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.models.Status;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.MedicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    
    private final LocalDateTime data = LocalDateTime.of(2030, 1, 1, 10, 0);

    @Test
    void getConsultaByMedicoId() {
        MedicoModel medico = criarMedico();

        ConsultaModel consultaModel = new ConsultaModel(null, data, "rotina", "consulta de rotina", Status.PENDENTE, medico, null);

    }

    @Test
    void getConsultaByClienteId() {
    }

    @Test
    @DisplayName("Deve retornar a consulta pelo id")
    void getConsultaById() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();

        UUID consultaId = UUID.randomUUID();
        ConsultaModel consulta = new  ConsultaModel(consultaId, data, "rotina", "consulta de rotina", Status.PENDENTE, medico, cliente);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));

        ConsultaResponseDto result = consultaService.getConsultaById(consultaId);

        verify(consultaRepository, times(1)).findById(consultaId);

        assertEquals(consulta.getId(), result.id());
        assertEquals(consulta.getDataConsulta(), result.data());
        assertEquals(consulta.getTipoConsulta(), result.tipo());
        assertEquals(consulta.getDescricaoConsulta(), result.descricao());
    }

    @Test
    @DisplayName("Não deve retornar consulta se ela não existir.")
    void getConsultaCase2() {
        UUID consultaId = UUID.randomUUID();

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ConsultaNotFoundException.class, () -> consultaService.getConsultaById(consultaId));
        assertEquals("Consulta não encontrada.", exception.getMessage());

        verify(consultaRepository, times(1)).findById(consultaId);
    }

    @Test
    @DisplayName("Deve criar a consulta.")
    void addConsultaCase1() {
        MedicoModel medico  = new MedicoModel();
        UUID medicoId = UUID.randomUUID();
        medico.setId(medicoId);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        ClienteModel cliente = new ClienteModel();
        UUID clienteId = UUID.randomUUID();
        cliente.setId(clienteId);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(false);


        ConsultaDto consultaDto = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);

        ConsultaModel saved = new ConsultaModel();
        saved.setId(UUID.randomUUID());
        saved.setDataConsulta(consultaDto.dataConsulta());
        saved.setTipoConsulta(consultaDto.tipoConsulta());
        saved.setDescricaoConsulta(consultaDto.descricaoConsulta());
        saved.setMedico(medico);
        saved.setCliente(cliente);

        when(consultaRepository.save(any())).thenReturn(saved);

        ConsultaResponseDto result = consultaService.addConsulta(consultaDto);

        verify(consultaRepository, times(1)).save(any());

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(result.data(), consultaDto.dataConsulta());
        assertEquals(result.tipo(), consultaDto.tipoConsulta());
        assertEquals(result.medicoId(), consultaDto.medico_id());
        assertEquals(result.clienteId(), consultaDto.cliente_id());
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o cliente não existir.")
    void addConsultaCase2() {
        MedicoModel medico  = new MedicoModel();
        UUID medicoId = UUID.randomUUID();
        medico.setId(medicoId);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        UUID clienteId = UUID.randomUUID();

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        ConsultaDto consultaDto = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);

        Exception exception = assertThrows(ClienteNotFoundException.class, () -> consultaService.addConsulta(consultaDto));
        assertEquals("Cliente não encontrado.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o medico não existir.")
    void addConsultaCase3() {
        UUID medicoId = UUID.randomUUID();

        ClienteModel cliente = new ClienteModel();
        UUID clienteId = UUID.randomUUID();
        cliente.setId(clienteId);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

        ConsultaDto consultaDto = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);

        Exception exception = assertThrows(MedicoNotFoundException.class, () -> consultaService.addConsulta(consultaDto));
        assertEquals("Medico não encontrado.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o medico possuir outra no mesmo horário.")
    void addConsultaCase4(){
        MedicoModel medico  = new MedicoModel();
        UUID medicoId = UUID.randomUUID();
        medico.setId(medicoId);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        ClienteModel cliente = new ClienteModel();
        UUID clienteId = UUID.randomUUID();
        cliente.setId(clienteId);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(true);

        ConsultaDto consultaDto = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.addConsulta(consultaDto));

        assertEquals("Médico já possui uma consulta no horário.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve criar a consulta quando o cliente possuir outra no mesmo horário.")
    void  addConsultaCase5(){
        MedicoModel medico  = new MedicoModel();
        UUID medicoId = UUID.randomUUID();
        medico.setId(medicoId);
        medico.setNome("Jorge");
        medico.setCrm("4325");
        medico.setTelefone("(71)99999-9999");
        medico.setEspecialidade("Urologista");

        ClienteModel cliente = new ClienteModel();
        UUID clienteId = UUID.randomUUID();
        cliente.setId(clienteId);
        cliente.setNome("Fernando");
        cliente.setCpf("12345678901");
        cliente.setTelefone("(71)99999-9999");

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.of(medico));
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetween(any(), any(), any())).thenReturn(true);

        ConsultaDto consultaDto = new ConsultaDto(data,  "Rotina", "Consulta de rotina", medicoId, clienteId);

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.addConsulta(consultaDto));

        assertEquals("Cliente já possui uma consulta no horário.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Deve atualizar a consulta.")
    void updateConsultaCase1() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();

        UUID consultaId = UUID.randomUUID();
        ConsultaModel consulta = new ConsultaModel(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, medico, cliente);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(false);
        when(consultaRepository.save(any())).thenReturn(consulta);

        ConsultaUpdateDto consultaUpdate = new ConsultaUpdateDto(data.plusDays(1), "Nova Rotina", "Nova Consulta Rotina");

        ConsultaResponseDto result = consultaService.updateConsulta(consultaId, consultaUpdate);

        verify(consultaRepository, times(1)).save(any(ConsultaModel.class));

        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(consultaUpdate.tipo(), result.tipo());
        assertEquals(consultaUpdate.data(), result.data());
        assertEquals(consultaUpdate.descricao(), result.descricao());
    }

    @Test
    @DisplayName("Não deve atualizar a consulta se a consulta não existe.")
    void updateConsultaCase2() {
        UUID consultaId = UUID.randomUUID();
        ConsultaUpdateDto consultaUpdate = new ConsultaUpdateDto(data, "Tipo", "Descrição");

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ConsultaNotFoundException.class, () -> consultaService.updateConsulta(consultaId, consultaUpdate));
        assertEquals("Consulta não encontrada.", exception.getMessage());
        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve atualizar a consulta se o médico possuir outra no horário.")
    void updateConsultaCase3() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();
        UUID consultaId = UUID.randomUUID();

        ConsultaModel consulta = new ConsultaModel(consultaId, data, "Rotina", "Consulta de rotina", Status.PENDENTE, medico, cliente);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(true);

        ConsultaUpdateDto consultaDto = new ConsultaUpdateDto(data.plusDays(1), "`Nova Rotina", "Nova Consulta de rotina");

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.updateConsulta(consultaId, consultaDto));
        assertEquals("Conflito no horário da consulta.", exception.getMessage());

        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Não deve atualizar a consulta se o cliente possuir outra no horário.")
    void updateConsultaCase4() {
        MedicoModel medico = criarMedico();
        ClienteModel cliente = criarCliente();

        UUID consultaId = UUID.randomUUID();
        ConsultaModel consulta = new ConsultaModel(consultaId, data, "Rotina", "Consulta de rotina", Status.PENDENTE, medico, cliente);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));
        when(consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(false);
        when(consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(any(), any(), any(), any())).thenReturn(true);

        ConsultaUpdateDto consultaDto = new ConsultaUpdateDto(data.plusDays(1), "`Nova Rotina", "Nova Consulta de rotina");

        Exception exception = assertThrows(ConflitoConsultaException.class, () -> consultaService.updateConsulta(consultaId, consultaDto));
        assertEquals("Conflito no horário da consulta.", exception.getMessage());

        verify(consultaRepository, never()).save(any(ConsultaModel.class));
    }

    @Test
    @DisplayName("Deve alterar o status da consulta.")
    void alterarStatusConsulta() {
        UUID consultaId = UUID.randomUUID();
        ConsultaModel consulta = new ConsultaModel(consultaId, data, "Rotina", "Consulta de rotina", Status.PENDENTE, null, null);
        ArgumentCaptor<ConsultaModel> consultaCaptor = ArgumentCaptor.forClass(ConsultaModel.class);

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.of(consulta));

        consultaService.alterarStatusConsulta(consultaId, Status.CONCLUIDA);
        verify(consultaRepository).save(consultaCaptor.capture());
        ConsultaModel consultaModel = consultaCaptor.getValue();

        assertEquals(Status.CONCLUIDA, consultaModel.getStatus());
    }

    @Test
    @DisplayName("Não deve alterar o status da consulta se não encontrar nenhuma.")
    void alterarStatusConsultaCase2(){
        UUID consultaId = UUID.randomUUID();

        when(consultaRepository.findById(consultaId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ConsultaNotFoundException.class, () -> consultaService.alterarStatusConsulta(consultaId, Status.PENDENTE));

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