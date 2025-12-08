package com.example.consultas.controllers;

import com.example.consultas.dtos.medico.MedicoRequestDto;
import com.example.consultas.dtos.medico.MedicoResponseDto;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.repositories.UsuarioRepository;
import com.example.consultas.security.TokenService;
import com.example.consultas.services.MedicoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicoController.class)
@AutoConfigureMockMvc(addFilters = false)
class MedicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    MedicoService medicoService;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar o dto do usuário criado e o código Http created 201.")
    void addMedico() throws Exception {
        MedicoRequestDto medicoRequest = gerarMedicoRequest();
        MedicoResponseDto medicoResponse = gerarMedicoResponse();

        when(medicoService.addMedico(eq(medicoRequest))).thenReturn(medicoResponse);
        mockMvc.perform(post("/api/v1/medicos").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(medicoRequest))).andDo(print()).andExpectAll(status().isCreated(),
                jsonPath("$.id").value(medicoResponse.id().toString()),
                jsonPath("$.nome").value(medicoResponse.nome()),
                jsonPath("$.email").value(medicoResponse.email()),
                jsonPath("$.crm").value(medicoResponse.crm()),
                jsonPath("$.telefone").value(medicoResponse.telefone()),
                jsonPath("$.especialidade").value(medicoResponse.especialidade()),
                header().exists("Location"),
                content().contentType(MediaType.APPLICATION_JSON));

        verify(medicoService).addMedico(medicoRequest);
    }

    @Test
    @DisplayName("Deve retornar o código http BAD_REQUEST (400).")
    void addMedicoBadRequest() throws Exception {
        MedicoRequestDto medicoRequest = gerarMedicoRequest();

        EntityExistsException e = new EntityExistsException("CRM já cadastrado.");

        when(medicoService.addMedico(eq(medicoRequest))).thenThrow(e);

        mockMvc.perform(post("/api/v1/medicos").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medicoRequest)))
                .andExpectAll(status().isBadRequest(), content().string(containsString(e.getMessage())));

        verify(medicoService).addMedico(any(MedicoRequestDto.class));
    }

    @Test
    @DisplayName("Deve retornar código Http OK (200) e o dto do usuário encontrado.")
    void getMedicoById() throws Exception {
        MedicoResponseDto medicoResponse = gerarMedicoResponse();

        when(medicoService.getMedicoById(medicoResponse.id())).thenReturn(medicoResponse);

        mockMvc.perform(get("/api/v1/medicos/{id}", medicoResponse.id())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(medicoResponse.id().toString()),
                        jsonPath("$.nome").value(medicoResponse.nome()),
                        jsonPath("$.email").value(medicoResponse.email()),
                        jsonPath("$.crm").value(medicoResponse.crm()),
                        jsonPath("$.telefone").value(medicoResponse.telefone()),
                        jsonPath("$.especialidade").value(medicoResponse.especialidade()),
                        content().contentType(MediaType.APPLICATION_JSON));


        verify(medicoService).getMedicoById(medicoResponse.id());
    }

    @Test
    @DisplayName("Deve retornar código Http NOT_FOUND (404).")
    void getMedicoByIdNotFound() throws Exception {
        UUID medicoId = UUID.randomUUID();

        MedicoNotFoundException e = new MedicoNotFoundException();

        when(medicoService.getMedicoById(medicoId)).thenThrow(e);

        mockMvc.perform(get("/api/v1/medicos/{id}", medicoId)).andExpectAll(
                status().isNotFound(),
                content().string(containsString(e.getMessage()))
        );

        verify(medicoService).getMedicoById(medicoId);
    }

    @Test
    void getAllMedicos() {
        MedicoResponseDto medico1 = gerarMedicoResponse();
        MedicoResponseDto medico2 = gerarMedicoResponse();

        Page<MedicoResponseDto> medicosDtos = new PageImpl<>(List.of(medico1, medico2));
    }

    @Test
    @DisplayName("Deve retornar status OK (200) quando atualizar o médico.")
    void updateMedico() throws Exception {
        MedicoRequestDto medicoRequest = gerarMedicoRequest();
        MedicoResponseDto medicoResponse = gerarMedicoResponse();

        when(medicoService.updateMedico(eq(medicoResponse.id()), eq(medicoRequest))).thenReturn(medicoResponse);

        mockMvc.perform(put("/api/v1/medicos/{id}", medicoResponse.id()).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(medicoRequest))).andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(medicoResponse.id().toString()),
                jsonPath("$.nome").value(medicoResponse.nome()),
                jsonPath("$.email").value(medicoResponse.email()),
                jsonPath("$.crm").value(medicoResponse.crm()),
                jsonPath("$.telefone").value(medicoResponse.telefone()),
                jsonPath("$.especialidade").value(medicoResponse.especialidade())
                , content().contentType(MediaType.APPLICATION_JSON));

        verify(medicoService).updateMedico(medicoResponse.id(), medicoRequest);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND (404) ao tentar atualizar médico inexistente.")
    void updateMedicoNotFound() throws Exception {
        MedicoNotFoundException e = new MedicoNotFoundException();
        UUID medico_id = UUID.randomUUID();
        MedicoRequestDto medicoRequest = gerarMedicoRequest();

        when(medicoService.updateMedico(eq(medico_id), eq(medicoRequest))).thenThrow(e);

        mockMvc.perform(put("/api/v1/medicos/{id}", medico_id).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(medicoRequest)))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(e.getMessage()))
                );

        verify(medicoService).updateMedico(eq(medico_id), eq(medicoRequest));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST (400) quando tentar atualizar o email para um que já está em uso.")
    void updateMedicoEmailInvalido() throws Exception {
        EntityExistsException e = new EntityExistsException("Email já cadastrado.");
        UUID medico_id = UUID.randomUUID();
        MedicoRequestDto medicoRequest = gerarMedicoRequest();

        when(medicoService.updateMedico(eq(medico_id), eq(medicoRequest))).thenThrow(e);

        mockMvc.perform(put("/api/v1/medicos/{id}", medico_id).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(medicoRequest)))
                .andExpectAll(
                        status().isBadRequest(),
                        content().string(containsString(e.getMessage())));

        verify(medicoService).updateMedico(eq(medico_id), eq(medicoRequest));
    }

    @Test
    @DisplayName("Deve retornar NO_CONTENT (204) ao deletar o usuário.")
    void deleteMedico() throws Exception {
        UUID medico_id = UUID.randomUUID();

        doNothing().when(medicoService).deleteMedico(medico_id);

        mockMvc.perform(delete("/api/v1/medicos/{id}", medico_id)).andExpect(status().isNoContent());

        verify(medicoService).deleteMedico(medico_id);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND (404) ao tentar deletar usuário inexistente.")
    void deleteMedicoNotFound() throws Exception {
        UUID medico_id = UUID.randomUUID();
        MedicoNotFoundException e = new MedicoNotFoundException();

        doThrow(e).when(medicoService).deleteMedico(medico_id);

        mockMvc.perform(delete("/api/v1/medicos/{id}", medico_id)).andExpect(status().isNotFound());

        verify(medicoService).deleteMedico(medico_id);
    }

    private MedicoRequestDto gerarMedicoRequest() {
        return new MedicoRequestDto("Claudio", "123456", "claudio.silva@gmail.com", "12345Cs@", "(71)99999-9999", "Clinico Geral");
    }

    private MedicoResponseDto gerarMedicoResponse() {
        return new MedicoResponseDto(UUID.randomUUID(), "Claudio", "123456", "claudio.silva@gmail.com", "(71)99999-9999", "Clinico Geral");
    }

}