package com.example.consultas.services;

import com.example.consultas.dtos.cliente.ClienteRequestDto;
import com.example.consultas.dtos.cliente.ClienteResponseDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.UsuarioInativoException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.EnderecoRepository;
import com.example.consultas.repositories.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    EnderecoRepository enderecoRepository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    ConsultaRepository consultaRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    ClienteService clienteService;


    @Test
    @DisplayName("Deve gerar um cliente.")
    void addCliente() {
        UsuarioModel usuario = gerarUsuario();
        ClienteModel cliente = gerarCliente();
        cliente.setUsuario(usuario);
        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", "claudio@gmail.com", "1234", "12345678901", "(71)99999-9999");
        String senhaCriptografada = "senha-criptografada";
        ArgumentCaptor<UsuarioModel> usuarioArgument = ArgumentCaptor.forClass(UsuarioModel.class);

        when(usuarioRepository.existsByEmail(clienteDto.email())).thenReturn(false);
        when(clienteRepository.existsByCpf(clienteDto.cpf())).thenReturn(false);
        when(clienteRepository.save(any())).thenReturn(cliente);
        when(passwordEncoder.encode(clienteDto.senha())).thenReturn(senhaCriptografada);

        ClienteResponseDto result = clienteService.addCliente(clienteDto);

        verify(usuarioRepository).existsByEmail(clienteDto.email());
        verify(clienteRepository).existsByCpf(clienteDto.cpf());
        verify(clienteRepository).save(any(ClienteModel.class));


        verify(usuarioRepository).save(usuarioArgument.capture());
        UsuarioModel usuarioResult = usuarioArgument.getValue();

        assertNotNull(result);
        assertNotNull(result.id());
        assertNotNull(usuarioResult);
        assertEquals(clienteDto.cpf(), result.cpf());
        assertEquals(clienteDto.nome(), result.nome());
        assertEquals(clienteDto.telefone(), result.telefone());
        assertEquals(clienteDto.email(), usuarioResult.getEmail());
        assertEquals(senhaCriptografada, usuarioResult.getSenha());
    }

    @Test
    @DisplayName("Não deve gerar um cliente ao inserir um email existente.")
    void gerarClienteEmailExiste() {
        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", "claudio@gmail.com", "1234", "12345678901", "(71)99999-9999");

        when(usuarioRepository.existsByEmail(clienteDto.email())).thenReturn(true);

        Exception exception = assertThrows(EntityExistsException.class, () -> clienteService.addCliente(clienteDto));
        assertEquals("Email já cadastrado.", exception.getMessage());

        verify(usuarioRepository).existsByEmail(clienteDto.email());
    }

    @Test
    @DisplayName("Não deve gerar um cliente ao inserir um cpf existente.")
    void gerarClienteCpfExiste() {
        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", "claudio@gmail.com", "1234", "12345678901", "(71)99999-9999");

        when(usuarioRepository.existsByEmail(clienteDto.email())).thenReturn(false);
        when(clienteRepository.existsByCpf(clienteDto.cpf())).thenReturn(true);

        Exception exception = assertThrows(EntityExistsException.class, () -> clienteService.addCliente(clienteDto));
        assertEquals("CPF já cadastrado.", exception.getMessage());

        verify(usuarioRepository).existsByEmail(clienteDto.email());
        verify(clienteRepository).existsByCpf(clienteDto.cpf());
    }

    @Test
    @DisplayName("Deve atualizar cliente e o email do usuário relacionado.")
    void updateCliente() {
        ClienteModel cliente = gerarCliente();
        UsuarioModel usuario = gerarUsuario();
        cliente.setUsuario(usuario);

        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", "claudio2@gmail.com", null, "12345678901", "(71)99999-9999");

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(usuarioRepository.existsByEmail(clienteDto.email())).thenReturn(false);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        ClienteResponseDto result = clienteService.updateCliente(cliente.getId(), clienteDto);

        assertNotNull(result);
        assertEquals(cliente.getId(), result.id());
        assertEquals(clienteDto.cpf(), result.cpf());
        assertEquals(clienteDto.nome(), result.nome());
        assertEquals(clienteDto.telefone(), result.telefone());
        assertEquals(clienteDto.email(), result.email());

        verify(clienteRepository).findById(cliente.getId());
        verify(usuarioRepository).existsByEmail(clienteDto.email());
        verify(usuarioRepository).save(usuario);
        verify(clienteRepository).save(cliente);
    }

    @Test
    @DisplayName("Não deve atualizar nada do cliente se email inserido já existe.")
    void updateClienteEmailExiste() {
        ClienteModel cliente = gerarCliente();
        UsuarioModel usuario = gerarUsuario();
        cliente.setUsuario(usuario);

        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", "claudio2@gmail.com", null, "12345678901", "(71)99999-9999");

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(usuarioRepository.existsByEmail(clienteDto.email())).thenReturn(true);

        Exception exception = assertThrows(EntityExistsException.class, () -> clienteService.updateCliente(cliente.getId(), clienteDto));
        assertEquals("Email já cadastrado.", exception.getMessage());

        verify(clienteRepository).findById(cliente.getId());
        verify(usuarioRepository).existsByEmail(clienteDto.email());
    }

    @Test
    @DisplayName("Não deve atualizar nada do cliente se ele não existir.")
    void updateClienteNotFound() {
        UUID clienteId = UUID.randomUUID();
        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", "claudio2@gmail.com", null, "12345678901", "(71)99999-9999");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ClienteNotFoundException.class, () -> clienteService.updateCliente(clienteId, clienteDto));
        assertEquals("Cliente não encontrado.", exception.getMessage());

        verify(clienteRepository).findById(clienteId);
    }

    @Test
    @DisplayName("Não deve atualizar o cliente se ele estiver desativado.")
    void updateClienteDesativado() {
        UUID clienteId = UUID.randomUUID();
        ClienteModel cliente = gerarCliente();
        cliente.getUsuario().setEnabled(false);
        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", "claudio2@gmail.com", null, "12345678901", "(71)99999-9999");

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

        Exception exception = assertThrows(UsuarioInativoException.class, () -> clienteService.updateCliente(clienteId, clienteDto));
        assertEquals("Usuário inativo", exception.getLocalizedMessage());
    }

    @Test
    @DisplayName("Deve atualizar o cliente e a senha de usuário.")
    void updateClienteAtualizarSenha() {
        ClienteModel cliente = gerarCliente();
        UsuarioModel usuario = gerarUsuario();
        cliente.setUsuario(usuario);
        ArgumentCaptor<UsuarioModel> usuarioCaptor = ArgumentCaptor.forClass(UsuarioModel.class);

        String email = usuario.getEmail();
        String senhaCriptografada = "novaSenhaCriptografada";

        ClienteRequestDto clienteDto = new ClienteRequestDto("Claudio", null, "senha", "12345678901", "(71)99999-9999");

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode(clienteDto.senha())).thenReturn(senhaCriptografada);
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        ClienteResponseDto result = clienteService.updateCliente(cliente.getId(), clienteDto);

        verify(usuarioRepository).save(usuarioCaptor.capture());
        UsuarioModel usuarioResult = usuarioCaptor.getValue();

        verify(clienteRepository).findById(cliente.getId());
        verify(passwordEncoder).encode(clienteDto.senha());
        verify(usuarioRepository, never()).existsByEmail(clienteDto.email());
        verify(clienteRepository).save(cliente);

        assertNotNull(result);
        assertNotNull(result.email());
        assertEquals(cliente.getId(), result.id());
        assertEquals(clienteDto.cpf(), result.cpf());
        assertEquals(clienteDto.nome(), result.nome());
        assertEquals(clienteDto.telefone(), result.telefone());
        assertEquals(email, result.email());
        assertEquals(senhaCriptografada, usuarioResult.getSenha());
    }

    @Test
    @DisplayName("Deve retornar o cliente encontrado.")
    void getClienteById() {
        ClienteModel cliente = gerarCliente();
        UsuarioModel usuario = gerarUsuario();
        cliente.setUsuario(usuario);

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));

        ClienteResponseDto result = clienteService.getClienteById(cliente.getId());

        assertEquals(cliente.getId(), result.id());
        assertEquals(cliente.getNome(), result.nome());
        assertEquals(cliente.getCpf(), result.cpf());
        assertEquals(cliente.getTelefone(), result.telefone());
        assertEquals(usuario.getEmail(), result.email());

        verify(clienteRepository).findById(cliente.getId());
    }

    @Test
    @DisplayName("Não deve retornar nenhum cliente.")
    void getClienteByIdNotFound() {
        UUID clienteId = UUID.randomUUID();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ClienteNotFoundException.class, () -> clienteService.getClienteById(clienteId));
        assertEquals("Cliente não encontrado.", exception.getMessage());

        verify(clienteRepository).findById(clienteId);
    }

    @Test
    void getClienteByNome() {
    }

    @Test
    @DisplayName("Retorna um page com todos os clientes")
    void getAllClientes() {
        ClienteModel c1 = gerarCliente();
        ClienteModel c2 = gerarCliente();
        ClienteModel c3 = gerarCliente();


        Page<ClienteModel> clientes = new PageImpl<>(Arrays.asList(c1, c2, c3));

        Pageable pageable = PageRequest.of(0, 10);
        when(clienteRepository.findAll(pageable)).thenReturn(clientes);

        Page<ClienteResponseDto> result = clienteService.getAllClientes(pageable);

        assertEquals(3, result.getSize());
        assertEquals(1, result.getTotalPages());
        assertEquals(c1.getId(), result.getContent().getFirst().id());
        assertEquals(c1.getNome(), result.getContent().getFirst().nome());
        assertEquals(c2.getId(), result.getContent().get(1).id());
        assertEquals(c2.getNome(), result.getContent().get(1).nome());
        assertEquals(c3.getId(), result.getContent().get(2).id());
        assertEquals(c3.getNome(), result.getContent().get(2).nome());

        verify(clienteRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve desativar um cliente, o usuário relacionado e seus endereços.")
    void desativarCliente() {
        ClienteModel cliente = gerarCliente();
        UsuarioModel usuario = gerarUsuario();
        cliente.setUsuario(usuario);

        when(clienteRepository.findById(cliente.getId())).thenReturn(Optional.of(cliente));

        clienteService.desativarCliente(cliente.getId());

        assertEquals(false, usuario.getEnabled());
        verify(clienteRepository).findById(cliente.getId());
    }

    @Test
    @DisplayName("Não deve desativar um cliente se ele não existir.")
    void desativarClienteNotFound() {
        UUID clienteId = UUID.randomUUID();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ClienteNotFoundException.class, () -> clienteService.desativarCliente(clienteId));
        assertEquals("Cliente não encontrado.", exception.getMessage());
    }

    private ClienteModel gerarCliente() {
        return new ClienteModel(UUID.randomUUID(), "Claudio", "12345678901", "(71)99999-9999", null, gerarUsuario());
    }

    private UsuarioModel gerarUsuario() {
        return new UsuarioModel(UUID.randomUUID(), "claudio@gmail.com", "12345", Roles.CLIENTE, true, null, null, null);
    }
}