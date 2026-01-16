package com.example.consultas.controllers;

import com.example.consultas.dtos.consulta.ConsultaDto;
import com.example.consultas.dtos.consulta.ConsultaResponseDto;
import com.example.consultas.dtos.consulta.ConsultaResponseDto;
import com.example.consultas.dtos.consulta.ConsultaUpdateDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.ConflitoConsultaException;
import com.example.consultas.exceptions.ConsultaNotFoundException;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.Status;
import com.example.consultas.repositories.UsuarioRepository;
import com.example.consultas.security.TokenService;
import com.example.consultas.services.ConsultaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConsultaService consultaService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    ObjectMapper objectMapper;

    private LocalDateTime data = LocalDateTime.of(2026, 2, 21, 14, 0);

    @Test
    @DisplayName("Deve retornar status CREATED (201) e o dto da consulta criada.")
    void addConsulta() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        ConsultaDto consultaRequest = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);
        ConsultaResponseDto consultaResponse = new ConsultaResponseDto(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, medicoId, clienteId);

        when(consultaService.addConsulta(any(ConsultaDto.class))).thenReturn(consultaResponse);

        mockMvc.perform(post("/api/v1/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaRequest)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id", is(consultaResponse.id().toString())),
                        jsonPath("$.data", startsWith(data.toString())),
                        jsonPath("$.tipo", is(consultaResponse.tipo())),
                        jsonPath("$.descricao", is(consultaResponse.descricao())),
                        jsonPath("$.status", is(consultaResponse.status().toString())),
                        jsonPath("$.medicoId", is(medicoId.toString())),
                        jsonPath("$.clienteId", is(clienteId.toString()))
                );

        verify(consultaService).addConsulta(any(ConsultaDto.class));
    }

    @Test
    @DisplayName("Deve retornar status NOT_FOUND (404) ao tentar criar uma consulta para um médico inexistente.")
    void addConsultaMedicoNotFound() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        ConsultaDto consultaRequest = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);
        MedicoNotFoundException e = new MedicoNotFoundException();

        when(consultaService.addConsulta(any(ConsultaDto.class))).thenThrow(e);

        mockMvc.perform(post("/api/v1/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaRequest)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())),
                        jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas"))
                );

        verify(consultaService).addConsulta(any(ConsultaDto.class));
    }

    @Test
    @DisplayName("Deve retornar status NOT_FOUND (404) ao tentar criar uma consulta com um cliente inexistente.")
    void addConsultaClienteNotFound() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        ConsultaDto consultaRequest = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);
        ClienteNotFoundException e = new ClienteNotFoundException();

        when(consultaService.addConsulta(any(ConsultaDto.class))).thenThrow(e);

        mockMvc.perform(post("/api/v1/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaRequest)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())),
                        jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas"))
                );

        verify(consultaService).addConsulta(any(ConsultaDto.class));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST (400) quando cliente ou médico tiver alguma consulta no mesmo horário.")
    void addConsultaConflitoNoHorario() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        ConsultaDto consultaRequest = new ConsultaDto(data, "Rotina", "Consulta de rotina", medicoId, clienteId);
        ConflitoConsultaException e = new ConflitoConsultaException();

        when(consultaService.addConsulta(any(ConsultaDto.class))).thenThrow(e);

        mockMvc.perform(post("/api/v1/consultas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaRequest)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status", is(HttpStatus.CONFLICT.value())),
                        jsonPath("$.error", is(HttpStatus.CONFLICT.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas"))
                );

        verify(consultaService).addConsulta(any(ConsultaDto.class));
    }


    @Test
    @DisplayName("Deve retornar status OK (200) e o dto da consulta.")
    void getConsulta() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        ConsultaResponseDto consultaResponse = new ConsultaResponseDto(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, medicoId, clienteId);

        when(consultaService.getConsultaById(consultaResponse.id())).thenReturn(consultaResponse);

        mockMvc.perform(get("/api/v1/consultas/{id}", consultaResponse.id())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", is(consultaResponse.id().toString())),
                        jsonPath("$.data", startsWith(data.toString())),
                        jsonPath("$.tipo", is(consultaResponse.tipo())),
                        jsonPath("$.descricao", is(consultaResponse.descricao())),
                        jsonPath("$.status", is(consultaResponse.status().toString())),
                        jsonPath("$.medicoId", is(medicoId.toString())),
                        jsonPath("$.clienteId", is(clienteId.toString()))
                );

        verify(consultaService).getConsultaById(consultaResponse.id());
    }

    @Test
    @DisplayName("Deve retornar status NOT_FOUND (404) ao tentar buscar uma consulta inexistente.")
    void getConsultaNotFound() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID consultaId = UUID.randomUUID();
        ConsultaNotFoundException e = new ConsultaNotFoundException();

        when(consultaService.getConsultaById(consultaId)).thenThrow(e);

        mockMvc.perform(get("/api/v1/consultas/{id}", consultaId))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())),
                        jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas/" + consultaId))
                );

        verify(consultaService).getConsultaById(consultaId);
    }

    @Test
    @DisplayName("Deve retornar status OK (200) e as consultas do médico.")
    void getConsultaByMedicoId() throws Exception {
        UUID medicoId = UUID.randomUUID();
        ConsultaResponseDto c1 = new ConsultaResponseDto(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, medicoId, UUID.randomUUID());
        ConsultaResponseDto c2 = new ConsultaResponseDto(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, medicoId, UUID.randomUUID());

        Page<ConsultaResponseDto> consultas = new PageImpl<>(List.of(c1, c2));

        when(consultaService.getConsultaByMedicoId(eq(medicoId), any(Pageable.class))).thenReturn(consultas);

        mockMvc.perform(get("/api/v1/consultas/medico/{medico_id}", medicoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content[0].id", is(c1.id().toString())),
                        jsonPath("$.content[0].medicoId", is(medicoId.toString())),
                        jsonPath("$.content[0].links[0].href", is("http://localhost/api/v1/consultas/" + c1.id().toString())),
                        jsonPath("$.content[1].id", is(c2.id().toString())),
                        jsonPath("$.content[1].medicoId", is(medicoId.toString())),
                        jsonPath("$.content[1].links[0].href", is("http://localhost/api/v1/consultas/" + c2.id().toString())),
                        jsonPath("$.totalPages", is(1)),
                        jsonPath("$.number", is(0)),
                        jsonPath("$.size", is(2))
                );

        verify(consultaService).getConsultaByMedicoId(eq(medicoId), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar status NOT_FOUND (404) ao tentar retornar as consultas de um médico inexistente.")
    void getConsultasMedicoNotFound() throws Exception {
        UUID medicoId = UUID.randomUUID();

        MedicoNotFoundException e = new MedicoNotFoundException();

        when(consultaService.getConsultaByMedicoId(eq(medicoId), any(Pageable.class))).thenThrow(e);

        mockMvc.perform(get("/api/v1/consultas/medico/{medico_id}", medicoId).param("page", "0").param("size", "10"))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())),
                        jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas/medico/" + medicoId))
                );

        verify(consultaService).getConsultaByMedicoId(eq(medicoId), any(Pageable.class));
    }

    @Test
    void getConsultaByClienteId() throws Exception {
        UUID clienteId = UUID.randomUUID();
        ConsultaResponseDto c1 = new ConsultaResponseDto(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, UUID.randomUUID(), clienteId);
        ConsultaResponseDto c2 = new ConsultaResponseDto(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, UUID.randomUUID(), clienteId);

        Page<ConsultaResponseDto> consultas = new PageImpl<>(List.of(c1, c2));

        when(consultaService.getConsultaByClienteId(eq(clienteId), any(Pageable.class))).thenReturn(consultas);

        mockMvc.perform(get("/api/v1/consultas/cliente/{cliente_id}", clienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content[0].id", is(c1.id().toString())),
                        jsonPath("$.content[0].links[0].href", is("http://localhost/api/v1/consultas/" + c1.id().toString())),
                        jsonPath("$.content[1].id", is(c2.id().toString())),
                        jsonPath("$.content[1].links[0].href", is("http://localhost/api/v1/consultas/" + c2.id().toString())),
                        jsonPath("$.totalPages", is(1)),
                        jsonPath("$.number", is(0)),
                        jsonPath("$.size", is(2))
                );

        verify(consultaService).getConsultaByClienteId(eq(clienteId), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar status NOT_FOUND (404) ao tentar retornar as consultas de um cliente inexistente.")
    void getConsultasClienteNotFound() throws Exception {
        UUID clienteId = UUID.randomUUID();

        ClienteNotFoundException e = new ClienteNotFoundException();

        when(consultaService.getConsultaByClienteId(eq(clienteId), any(Pageable.class))).thenThrow(e);

        mockMvc.perform(get("/api/v1/consultas/cliente/{cliente_id}", clienteId).param("page", "0").param("size", "10"))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())),
                        jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas/cliente/" + clienteId))
                );

        verify(consultaService).getConsultaByClienteId(eq(clienteId), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar status OK (200) ao atualizar a consulta e retornar o dto da consulta.")
    void updateConsulta() throws Exception {
        UUID medicoId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        ConsultaResponseDto consulta = new ConsultaResponseDto(UUID.randomUUID(), data, "Rotina", "Consulta de rotina", Status.PENDENTE, medicoId, clienteId);
        ConsultaUpdateDto consultaUpdate = new ConsultaUpdateDto(data, "Novo tipo", "Nova descrição");

        when(consultaService.updateConsulta(eq(consulta.id()), any(ConsultaUpdateDto.class))).thenReturn(consulta);

        mockMvc.perform(put("/api/v1/consultas/{id}", consulta.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultaUpdate)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", is(consulta.id().toString())),
                        jsonPath("$.data", startsWith(data.toString())),
                        jsonPath("$.tipo", is(consulta.tipo())),
                        jsonPath("$.descricao", is(consulta.descricao())),
                        jsonPath("$.status", is(consulta.status().toString()))
                );

        verify(consultaService).updateConsulta(eq(consulta.id()), any(ConsultaUpdateDto.class));
    }

    @Test
    @DisplayName("Deve retornar status NOT_FOUND (404) ao tentar atualizar uma consulta inexistente.")
    void updateConsultaNotFound() throws Exception {
        ConsultaUpdateDto consulta = new ConsultaUpdateDto(data, "Rotina", "Consulta de rotina");
        ConsultaNotFoundException e = new ConsultaNotFoundException();
        UUID consultaId = UUID.randomUUID();

        when(consultaService.updateConsulta(eq(consultaId), any(ConsultaUpdateDto.class))).thenThrow(e);

        mockMvc.perform(put("/api/v1/consultas/{id}", consultaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())),
                        jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas/" + consultaId))
                );

        verify(consultaService).updateConsulta(eq(consultaId), any(ConsultaUpdateDto.class));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST (400) ao tentar atualizar a consulta quando houver um conflito no horário.")
    void updateConsultaConflitoHorario() throws Exception {
        ConsultaUpdateDto consulta = new ConsultaUpdateDto(data, "Rotina", "Consulta de rotina");
        ConflitoConsultaException e = new ConflitoConsultaException();
        UUID consultaId = UUID.randomUUID();

        when(consultaService.updateConsulta(eq(consultaId), any(ConsultaUpdateDto.class))).thenThrow(e);

        mockMvc.perform(put("/api/v1/consultas/{id}", consultaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status", is(HttpStatus.CONFLICT.value())),
                        jsonPath("$.error", is(HttpStatus.CONFLICT.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas/" + consultaId))
                );

        verify(consultaService).updateConsulta(eq(consultaId), any(ConsultaUpdateDto.class));
    }

    @Test
    @DisplayName("Deve retornar status CONFLICT (409) ao tentar atualizar uma consulta cancelada/concluida")
    void  updateConsultaCancelada() throws Exception {
        ConsultaUpdateDto consulta = new ConsultaUpdateDto(data, "Rotina", "Consulta de rotina");
        UUID consultaId = UUID.randomUUID();
        ConflitoConsultaException e = new ConflitoConsultaException();

        when(consultaService.updateConsulta(eq(consultaId), any(ConsultaUpdateDto.class))).thenThrow(e);

        mockMvc.perform(put("/api/v1/consultas/{id}", consultaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consulta)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status", is(HttpStatus.CONFLICT.value())),
                        jsonPath("$.error", is(HttpStatus.CONFLICT.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas/" + consultaId))
                );
        verify(consultaService).updateConsulta(eq(consultaId), any(ConsultaUpdateDto.class));
    }


    @Test
    @DisplayName("Deve retornar status OK (201) ao alterar o status de uma consulta.")
    void alterarStatusConsulta() throws Exception {
        UUID consultaId = UUID.randomUUID();

        doNothing().when(consultaService).alterarStatusConsulta(eq(consultaId), eq(Status.CONCLUIDA));
        mockMvc.perform(patch("/api/v1/consultas/{id}", consultaId)
                .param("status", Status.CONCLUIDA.toString()))
                .andExpect(status().isNoContent());

        verify(consultaService).alterarStatusConsulta(eq(consultaId), eq(Status.CONCLUIDA));
    }

    @Test
    @DisplayName("Deve retornar status NOT_FOUND (404) ao tentar alterar o status de uma consulta inexistente.")
    void deleteConsultaNotFound() throws Exception {
        UUID consultaId = UUID.randomUUID();
        ConsultaNotFoundException e = new ConsultaNotFoundException();

        doThrow(e).when(consultaService).alterarStatusConsulta(eq(consultaId), eq(Status.CONCLUIDA));

        mockMvc.perform(patch("/api/v1/consultas/{id}", consultaId)
                        .param("status", Status.CONCLUIDA.toString()))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())),
                        jsonPath("$.error", is(HttpStatus.NOT_FOUND.name())),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/consultas/" + consultaId))
                );

        verify(consultaService).alterarStatusConsulta(eq(consultaId), eq(Status.CONCLUIDA));
    }
}