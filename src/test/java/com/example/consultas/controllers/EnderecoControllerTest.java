package com.example.consultas.controllers;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.exceptions.EnderecoNotFoundException;
import com.example.consultas.exceptions.UsuarioNotFoundException;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.UsuarioRepository;
import com.example.consultas.security.TokenService;
import com.example.consultas.services.EnderecoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnderecoController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnderecoControllerTest {

    @MockitoBean
    EnderecoService enderecoService;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar CREATED (201) e o dto do endereço criado")
    void criarEndereco() throws Exception {
        EnderecoDto enderecoRequest = gerarEndereco();
        EnderecoDto enderecoResponse = new EnderecoDto(UUID.randomUUID(), "BA", "Salvador", "41285942", "Brotas", "Rua Fonseca", "41");
        UsuarioModel usuario = gerarUsuario();

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(enderecoService.addEndereco(eq(enderecoRequest), eq(usuario.getId()))).thenReturn(enderecoResponse);

        mockMvc.perform(post("/api/v1/enderecos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enderecoRequest)))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id", is(enderecoResponse.id().toString())),
                        jsonPath("$.uf", is(enderecoResponse.uf())),
                        jsonPath("$.cidade", is(enderecoResponse.cidade())),
                        jsonPath("$.cep", is(enderecoResponse.cep())),
                        jsonPath("$.bairro", is(enderecoResponse.bairro())),
                        jsonPath("$.rua", is(enderecoResponse.rua())),
                        jsonPath("$.numero", is(enderecoResponse.numero()))
                );

        verify(enderecoService).addEndereco(eq(enderecoRequest), eq(usuario.getId()));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve retornar o código NOT_FOUND (404) ao tentar criar um endereço para um usuário inexistente.")
    void criarEnderecoUsuárioInexistente() throws Exception {
        EnderecoDto enderecoRequest = gerarEndereco();
        UsuarioModel usuario = gerarUsuario();

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        UsuarioNotFoundException e = new UsuarioNotFoundException();

        when(enderecoService.addEndereco(enderecoRequest, usuario.getId())).thenThrow(e);

        mockMvc.perform(post("/api/v1/enderecos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enderecoRequest)))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(e.getMessage()))
                );

        verify(enderecoService).addEndereco(enderecoRequest, usuario.getId());
        SecurityContextHolder.clearContext();

    }

    @Test
    @DisplayName("Deve retornar status OK (200) e o dto do endereço encontrado.")
    void encontrarEndereco() throws Exception {
        EnderecoDto enderecoResponse = gerarEndereco();

        when(enderecoService.getEndereco(enderecoResponse.id())).thenReturn(enderecoResponse);

        mockMvc.perform(get("/api/v1/enderecos/{id}", enderecoResponse.id()).contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", is(enderecoResponse.id().toString())),
                        jsonPath("$.uf", is(enderecoResponse.uf())),
                        jsonPath("$.cidade", is(enderecoResponse.cidade())),
                        jsonPath("$.cep", is(enderecoResponse.cep())),
                        jsonPath("$.bairro", is(enderecoResponse.bairro())),
                        jsonPath("$.rua", is(enderecoResponse.rua())),
                        jsonPath("$.numero", is(enderecoResponse.numero()))
                );

        verify(enderecoService).getEndereco(enderecoResponse.id());
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND (404) quando não encontrar um usuário.")
    void encontrarEnderecoNotFound() throws Exception {
        UUID enderecoId = UUID.randomUUID();

        EnderecoNotFoundException e = new EnderecoNotFoundException();

        when(enderecoService.getEndereco(enderecoId)).thenThrow(e);

        mockMvc.perform(get("/api/v1/enderecos/{id}", enderecoId))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(e.getMessage()))
                );

        verify(enderecoService).getEndereco(enderecoId);
    }

    @Test
    @DisplayName("Deve retornar status OK (200) e os endereços do usuário.")
    void encontrarEnderecosByUsuarioId() throws Exception {
        EnderecoDto e1 = gerarEndereco();
        EnderecoDto e2 = gerarEndereco();
        UsuarioModel usuario = gerarUsuario();

        Page<EnderecoDto> enderecos = new PageImpl<>(List.of(e1, e2));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(enderecoService.getAllEnderecoByUsuarioId(eq(usuario.getId()), isA(Pageable.class))).thenReturn(enderecos);

        mockMvc.perform(get("/api/v1/enderecos").contentType(MediaType.APPLICATION_JSON).param("page", "0").param("size", "10"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.content").isArray(),
                        jsonPath("$.content[0].id", is(e1.id().toString())),
                        jsonPath("$.content[1].id", is(e2.id().toString())),
                        jsonPath("$.totalPages", is(1)),
                        jsonPath("$.totalElements", is(2))
                );

        verify(enderecoService).getAllEnderecoByUsuarioId(eq(usuario.getId()), isA(Pageable.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve retornar OK (200) e o endereço atualizado.")
    void editarEndereco() throws Exception {
        EnderecoDto enderecoRequest = gerarEndereco();
        EnderecoDto enderecoResponse = new EnderecoDto(enderecoRequest.id(), "BA", "Salvador", "41285942", "Brotas", "Rua Fonseca", "41");
        ;

        when(enderecoService.updateEndereco(enderecoResponse.id(), enderecoRequest)).thenReturn(enderecoResponse);

        mockMvc.perform(put("/api/v1/enderecos/{id}", enderecoResponse.id()).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(enderecoRequest)))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id", is(enderecoResponse.id().toString())),
                        jsonPath("$.uf", is(enderecoResponse.uf())),
                        jsonPath("$.cidade", is(enderecoResponse.cidade())),
                        jsonPath("$.cep", is(enderecoResponse.cep())),
                        jsonPath("$.bairro", is(enderecoResponse.bairro())),
                        jsonPath("$.rua", is(enderecoResponse.rua())),
                        jsonPath("$.numero", is(enderecoResponse.numero()))
                );

        verify(enderecoService).updateEndereco(enderecoResponse.id(), enderecoRequest);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND (404) ao tentar editar um endereço inexistente")
    void editarEnderecoNotFound() throws Exception {
        UUID enderecoId = UUID.randomUUID();
        EnderecoDto enderecoRequest = gerarEndereco();
        EnderecoNotFoundException e = new EnderecoNotFoundException();

        when(enderecoService.updateEndereco(enderecoId, enderecoRequest)).thenThrow(e);

        mockMvc.perform(put("/api/v1/enderecos/{id}", enderecoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(enderecoRequest)))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(e.getMessage()))
                );

        verify(enderecoService).updateEndereco(enderecoId, enderecoRequest);
    }

    @Test
    @DisplayName("Deve retornar NO_CONTENT (204) ao deletar o endereço.")
    void deletarEndereco() throws Exception {
        UUID enderecoId = UUID.randomUUID();

        doNothing().when(enderecoService).deleteEndereco(enderecoId);

        mockMvc.perform(delete("/api/v1/enderecos/{id}", enderecoId)).andExpect(status().isNoContent());

        verify(enderecoService).deleteEndereco(enderecoId);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND (404) ao tentar deletar um endereço inexistente.")
    void deletarEnderecoNotFound() throws Exception {
        UUID enderecoId = UUID.randomUUID();
        EnderecoNotFoundException e = new EnderecoNotFoundException();

        doThrow(e).when(enderecoService).deleteEndereco(enderecoId);

        mockMvc.perform(delete("/api/v1/enderecos/{id}", enderecoId))
                .andExpectAll(
                        status().isNotFound(),
                        content().string(containsString(e.getMessage()))
                );
    }

    private EnderecoDto gerarEndereco() {
        return new EnderecoDto(UUID.randomUUID(), "BA", "Salvador", "41285942", "Brotas", "Rua Fonseca", "41");
    }

    private UsuarioModel gerarUsuario() {
        return new UsuarioModel(UUID.randomUUID(), "carlos@gmail.com", "12345", Roles.MEDICO, true, null);
    }
}