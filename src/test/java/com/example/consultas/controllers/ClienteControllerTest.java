package com.example.consultas.controllers;

import com.example.consultas.dtos.cliente.ClienteRequestDto;
import com.example.consultas.dtos.cliente.ClienteResponseDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.UsuarioInativoException;
import com.example.consultas.repositories.UsuarioRepository;
import com.example.consultas.security.TokenService;
import com.example.consultas.services.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    ClienteService clienteService;

    @MockitoBean
    TokenService tokenService;

    @MockitoBean
    UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve retornar o dto do usuário criado e o código Http created 201.")
    void addCliente() throws Exception {
        ClienteRequestDto clienteRequest = gerarClienteRequest();
        ClienteResponseDto clienteResponse = gerarClienteResponse();

        when(clienteService.addCliente(eq(clienteRequest))).thenReturn(clienteResponse);

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(clienteRequest))).andDo(print()).andExpectAll(status().isCreated(),
                jsonPath("$.id").value(clienteResponse.id().toString()),
                jsonPath("$.nome").value(clienteResponse.nome()),
                jsonPath("$.email").value(clienteResponse.email()),
                jsonPath("$.cpf").value(clienteResponse.cpf()),
                jsonPath("$.telefone").value(clienteResponse.telefone()),
                header().exists("Location"),
                content().contentType(MediaType.APPLICATION_JSON));

        verify(clienteService).addCliente(clienteRequest);
    }

    @Test
    @DisplayName("Deve retornar o código http CONFLICT (409).")
    void addClienteCpfCadastrado() throws Exception {
        ClienteRequestDto clienteRequest = gerarClienteRequest();

        EntityExistsException e = new EntityExistsException("CPF já cadastrado.");

        when(clienteService.addCliente(eq(clienteRequest))).thenThrow(e);

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status", is(409)),
                        jsonPath("$.error", is("CONFLICT")),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/clientes")));

        verify(clienteService).addCliente(ArgumentMatchers.any(ClienteRequestDto.class));
    }

    @Test
    @DisplayName("Deve retornar o código http CONFLICT (409).")
    void addClienteEmailCadastrado() throws Exception {
        ClienteRequestDto clienteRequest = gerarClienteRequest();

        EntityExistsException e = new EntityExistsException("Email já cadastrado.");

        when(clienteService.addCliente(eq(clienteRequest))).thenThrow(e);

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status", is(409)),
                        jsonPath("$.error", is("CONFLICT")),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/clientes")));

        verify(clienteService).addCliente(ArgumentMatchers.any(ClienteRequestDto.class));
    }

    @Test
    @DisplayName("Deve retornar código Http OK (200) e o dto do usuário encontrado.")
    void getClienteById() throws Exception {
        ClienteResponseDto clienteResponse = gerarClienteResponse();

        when(clienteService.getClienteById(clienteResponse.id())).thenReturn(clienteResponse);

        mockMvc.perform(get("/api/v1/clientes/{id}", clienteResponse.id())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.id").value(clienteResponse.id().toString()),
                        jsonPath("$.nome").value(clienteResponse.nome()),
                        jsonPath("$.email").value(clienteResponse.email()),
                        jsonPath("$.cpf").value(clienteResponse.cpf()),
                        jsonPath("$.telefone").value(clienteResponse.telefone()),
                        content().contentType(MediaType.APPLICATION_JSON));


        verify(clienteService).getClienteById(clienteResponse.id());
    }

    @Test
    @DisplayName("Deve retornar código Http NOT_FOUND (404).")
    void getClienteByIdNotFound() throws Exception {
        UUID clienteId = UUID.randomUUID();

        ClienteNotFoundException e = new ClienteNotFoundException();

        when(clienteService.getClienteById(clienteId)).thenThrow(e);

        mockMvc.perform(get("/api/v1/clientes/{id}", clienteId)).andExpectAll(
                status().isNotFound(),
                jsonPath("$.status", is(404)),
                jsonPath("$.error", is("NOT_FOUND")),
                jsonPath("$.message", is(e.getMessage())),
                jsonPath("$.path", is("/api/v1/clientes/" + clienteId)));

        verify(clienteService).getClienteById(clienteId);
    }

    @Test
    @DisplayName("Deve retornar status OK (200) ao retornar todos os clientes.")
    void getAllClientes() throws Exception {
        ClienteResponseDto cliente1 = gerarClienteResponse();
        ClienteResponseDto cliente2 = gerarClienteResponse();


        Page<ClienteResponseDto> clientesDtos = new PageImpl<>(List.of(cliente1, cliente2));

        when(clienteService.getAllClientes(ArgumentMatchers.any(Pageable.class))).thenReturn(clientesDtos);

        mockMvc.perform(get("/api/v1/clientes")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$._embedded.clienteResponseDtoList").isArray(),
                        jsonPath("$._links").exists(),
                        jsonPath("$._links.self").exists(),
                        jsonPath("$._links.self.href").value("http://localhost/api/v1/clientes?page=0&size=10"),
                        jsonPath("$._embedded.clienteResponseDtoList", hasSize(2)),
                        jsonPath("$._embedded.clienteResponseDtoList[0].id").value(cliente1.id().toString()),
                        jsonPath("$._embedded.clienteResponseDtoList[0]._links.self.href").exists(),
                        jsonPath("$._embedded.clienteResponseDtoList[1].id").value(cliente2.id().toString()),
                        jsonPath("$._embedded.clienteResponseDtoList[1]._links.self.href").exists());
        verify(clienteService).getAllClientes(any());
    }

    @Test
    @DisplayName("Deve retornar status OK (200) quando atualizar o médico.")
    void updateCliente() throws Exception {
        ClienteRequestDto clienteRequest = gerarClienteRequest();
        ClienteResponseDto clienteResponse = gerarClienteResponse();

        when(clienteService.updateCliente(eq(clienteResponse.id()), eq(clienteRequest))).thenReturn(clienteResponse);

        mockMvc.perform(put("/api/v1/clientes/{id}", clienteResponse.id()).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(clienteRequest))).andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(clienteResponse.id().toString()),
                jsonPath("$.nome").value(clienteResponse.nome()),
                jsonPath("$.email").value(clienteResponse.email()),
                jsonPath("$.cpf").value(clienteResponse.cpf()),
                jsonPath("$.telefone").value(clienteResponse.telefone())
                , content().contentType(MediaType.APPLICATION_JSON));

        verify(clienteService).updateCliente(clienteResponse.id(), clienteRequest);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND (404) ao tentar atualizar médico inexistente.")
    void updateClienteNotFound() throws Exception {
        ClienteNotFoundException e = new ClienteNotFoundException();
        UUID clienteId = UUID.randomUUID();
        ClienteRequestDto clienteRequest = gerarClienteRequest();

        when(clienteService.updateCliente(eq(clienteId), eq(clienteRequest))).thenThrow(e);

        mockMvc.perform(put("/api/v1/clientes/{id}", clienteId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.status", is(404)),
                        jsonPath("$.error", is("NOT_FOUND")),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/clientes/" + clienteId)));

        verify(clienteService).updateCliente(eq(clienteId), eq(clienteRequest));
    }

    @Test
    @DisplayName("Deve retornar CONFLICT (409) quando tentar atualizar o email para um que já está em uso.")
    void updateClienteEmailInvalido() throws Exception {
        EntityExistsException e = new EntityExistsException("Email já cadastrado.");
        UUID clienteId = UUID.randomUUID();
        ClienteRequestDto clienteRequest = gerarClienteRequest();

        when(clienteService.updateCliente(eq(clienteId), eq(clienteRequest))).thenThrow(e);

        mockMvc.perform(put("/api/v1/clientes/{id}", clienteId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.status", is(409)),
                        jsonPath("$.error", is("CONFLICT")),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/clientes/" + clienteId)));

        verify(clienteService).updateCliente(eq(clienteId), eq(clienteRequest));
    }

    @Test
    @DisplayName("Deve retornar BAD_REQUEST (400) ao tentar atualizar um cliente desativado.")
    void updateClienteDesativado() throws Exception {
        UsuarioInativoException e = new UsuarioInativoException();
        UUID clienteId = UUID.randomUUID();
        ClienteRequestDto clienteRequest = gerarClienteRequest();

        when(clienteService.updateCliente(eq(clienteId), eq(clienteRequest))).thenThrow(e);

        mockMvc.perform(put("/api/v1/clientes/{id}", clienteId).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(clienteRequest)))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.status", is(400)),
                        jsonPath("$.error", is("BAD_REQUEST")),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/clientes/" + clienteId)));

        verify(clienteService).updateCliente(eq(clienteId), eq(clienteRequest));

    }

    @Test
    @DisplayName("Deve retornar NO_CONTENT (204) ao desativar um cliente.")
    void desativarCliente() throws Exception {
        UUID clienteId = UUID.randomUUID();

        doNothing().when(clienteService).desativarCliente(clienteId);

        mockMvc.perform(delete("/api/v1/clientes/{id}", clienteId)).andExpect(status().isNoContent());

        verify(clienteService).desativarCliente(clienteId);
    }

    @Test
    @DisplayName("Deve retornar NOT_FOUND (404) ao tentar desativar um cliente inexistente.")
    void desativarClienteNotFound() throws Exception {
        UUID clienteId = UUID.randomUUID();
        ClienteNotFoundException e = new ClienteNotFoundException();

        doThrow(e).when(clienteService).desativarCliente(clienteId);

        mockMvc.perform(delete("/api/v1/clientes/{id}", clienteId)).andExpectAll
                (status().isNotFound(),
                        jsonPath("$.status", is(404)),
                        jsonPath("$.error", is("NOT_FOUND")),
                        jsonPath("$.message", is(e.getMessage())),
                        jsonPath("$.path", is("/api/v1/clientes/" + clienteId)));

        verify(clienteService).desativarCliente(clienteId);
    }

    private ClienteRequestDto gerarClienteRequest() {
        return new ClienteRequestDto("Claudio", "claudio.silva@gmail.com", "123456Cs@", "12345678901", "(71)99999-9999");
    }

    private ClienteResponseDto gerarClienteResponse() {
        return new ClienteResponseDto(UUID.randomUUID(), "Claudio", "claudio.silva@gmail.com", "12345678901", "(71)99999-9999");
    }
}