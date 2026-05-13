package com.example.consultas.services;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.exceptions.EnderecoNotFoundException;
import com.example.consultas.exceptions.UsuarioNotFoundException;
import com.example.consultas.models.EnderecoModel;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.EnderecoRepository;
import com.example.consultas.repositories.UsuarioRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {

    @Mock
    EnderecoRepository enderecoRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    EnderecoService enderecoService;

    @Test
    @DisplayName("Deve gerar um endereço.")
    void addEndereco() {
        EnderecoModel endereco = gerarEndereco();
        UsuarioModel usuario = gerarUsuario();
        endereco.setUsuario(usuario);
        ArgumentCaptor<EnderecoModel> enderecoCaptor = ArgumentCaptor.forClass(EnderecoModel.class);

        EnderecoDto enderecoDto = new EnderecoDto(null, "BA", "Salvador", "41853865", "Brotas", "Rua dos Bobos", "69", true);

        when(enderecoRepository.save(any(EnderecoModel.class))).thenReturn(endereco);
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));

        enderecoService.addEndereco(enderecoDto, usuario.getId());

        verify(usuarioRepository).findById(usuario.getId());
        verify(enderecoRepository).save(enderecoCaptor.capture());
        EnderecoModel result = enderecoCaptor.getValue();


        assertNotNull(result);
        assertEquals(enderecoDto.cep(), result.getCep());
        assertEquals(enderecoDto.rua(), result.getRua());
        assertEquals(enderecoDto.cidade(), result.getCidade());
        assertEquals(enderecoDto.bairro(), result.getBairro());
        assertEquals(enderecoDto.uf(), result.getUf());
        assertEquals(enderecoDto.numero(), result.getNumero());
        assertEquals(usuario, result.getUsuario());
    }

    @Test
    @DisplayName("Não deve gerar um endereço se o usuário não existir.")
    void addEnderecoNotFound() {
        UUID usuario_id = UUID.randomUUID();
        EnderecoDto enderecoDto = new EnderecoDto(null, "BA", "Salvador", "41853865", "Brotas", "Rua dos Bobos", "69", true);

        when(usuarioRepository.findById(usuario_id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UsuarioNotFoundException.class, () -> enderecoService.addEndereco(enderecoDto, usuario_id));
        assertEquals("Usuário não encontrado.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar o endereço.")
    void updateEndereco() {
        EnderecoModel endereco = gerarEndereco();
        EnderecoDto enderecoDto = new EnderecoDto(null, "BA", "Salvador", "41855976", "Rio Vermelho", "Rua Alegre", "21", true);
        ArgumentCaptor<EnderecoModel> enderecoCaptor = ArgumentCaptor.forClass(EnderecoModel.class);

        when(enderecoRepository.findById(endereco.getId())).thenReturn(Optional.of(endereco));
        when(enderecoRepository.save(any(EnderecoModel.class))).thenReturn(endereco);

        enderecoService.updateEndereco(endereco.getId(), enderecoDto);

        verify(enderecoRepository).findById(endereco.getId());
        verify(enderecoRepository).save(enderecoCaptor.capture());
        EnderecoModel result = enderecoCaptor.getValue();

        assertNotNull(result);
        assertEquals(enderecoDto.cep(), result.getCep());
        assertEquals(enderecoDto.rua(), result.getRua());
        assertEquals(enderecoDto.cidade(), result.getCidade());
        assertEquals(enderecoDto.bairro(), result.getBairro());
        assertEquals( enderecoDto.uf(), result.getUf());
        assertEquals(enderecoDto.numero(), result.getNumero());
    }

    @Test
    @DisplayName("Não deve atualizar o endereço se ele não existe.")
    void updateEnderecoNotFound() {
        UUID endereco_id = UUID.randomUUID();
        EnderecoDto enderecoDto = new EnderecoDto(null, "BA", "Salvador", "41855976", "Rio Vermelho", "Rua Alegre", "21", true);

        when(enderecoRepository.findById(endereco_id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EnderecoNotFoundException.class, () -> enderecoService.updateEndereco(endereco_id, enderecoDto));
        assertEquals("Endereço não encontrado.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar o endereço")
    void getEndereco() {
        EnderecoModel endereco = gerarEndereco();

        when(enderecoRepository.findById(endereco.getId())).thenReturn(Optional.of(endereco));

        EnderecoDto result = enderecoService.getEndereco(endereco.getId());

        verify(enderecoRepository).findById(endereco.getId());
        assertEquals(endereco.getId(), result.id());
        assertEquals(endereco.getUf(), result.uf());
        assertEquals(endereco.getRua(), result.rua());
        assertEquals(endereco.getNumero(), result.numero());
        assertEquals(endereco.getCep(), result.cep());
        assertEquals(endereco.getBairro(), result.bairro());
        assertEquals(endereco.getCidade(), result.cidade());
    }

    @Test
    @DisplayName("Não deve retornar o endereço se ele não existir.")
    void getEnderecoNotFound() {
        UUID endereco_id =  UUID.randomUUID();

        when(enderecoRepository.findById(endereco_id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EnderecoNotFoundException.class, () -> enderecoService.getEndereco(endereco_id));
        assertEquals("Endereço não encontrado.", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar endereços do usuário.")
    void getAllEndereco() {
        UsuarioModel usuario = gerarUsuario();
        Pageable pageable = PageRequest.of(0, 10);

        EnderecoModel endereco1 = gerarEndereco();
        endereco1.setUsuario(usuario);
        EnderecoModel endereco2 = gerarEndereco();
        endereco2.setUsuario(usuario);

        Page<EnderecoModel> enderecos = new PageImpl<>(List.of(endereco1 ,endereco2));
        when(enderecoRepository.findAllByUsuario_Id(usuario.getId(), pageable)).thenReturn(enderecos);

        Page<EnderecoDto> result = enderecoService.getAllEnderecoByUsuarioId(usuario.getId(), pageable);

        verify(enderecoRepository).findAllByUsuario_Id(usuario.getId(), pageable);

        assertEquals(2, result.getContent().size());
        assertEquals(enderecos.getTotalElements(), result.getTotalElements());
        assertEquals(enderecos.getTotalPages(), result.getTotalPages());

        assertEquals(endereco1.getId(), result.getContent().get(0).id());
        assertEquals(endereco2.getId(), result.getContent().get(1).id());
    }

    @Test
    @DisplayName("Deve retornar o único endereço encontrado.")
    void getAllEnderecoCase2(){
        UsuarioModel usuario = gerarUsuario();
        Pageable pageable = PageRequest.of(0, 10);
        EnderecoModel endereco = gerarEndereco();
        endereco.setUsuario(usuario);

        Page<EnderecoModel> enderecos = new PageImpl<>(List.of(endereco));

        when(enderecoRepository.findAllByUsuario_Id(usuario.getId(), pageable)).thenReturn(enderecos);

        Page<EnderecoDto> result = enderecoService.getAllEnderecoByUsuarioId(usuario.getId(), pageable);

        verify(enderecoRepository).findAllByUsuario_Id(usuario.getId(), pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(enderecos.getTotalElements(), result.getTotalElements());
        assertEquals(enderecos.getTotalPages(), result.getTotalPages());

        assertEquals(endereco.getId(), result.getContent().get(0).id());
        assertEquals(endereco.getCidade(), result.getContent().get(0).cidade());
        assertEquals(endereco.getUf(), result.getContent().get(0).uf());
        assertEquals(endereco.getRua(), result.getContent().get(0).rua());
        assertEquals(endereco.getNumero(), result.getContent().get(0).numero());
        assertEquals(endereco.getBairro(), result.getContent().get(0).bairro());
        assertEquals(endereco.getCep(), result.getContent().get(0).cep());
    }

    @Test
    @DisplayName("Deve remover o endereço.")
    void deleteEndereco() {
        EnderecoModel endereco = gerarEndereco();

        when(enderecoRepository.findById(endereco.getId())).thenReturn(Optional.of(endereco));

        enderecoService.deleteEndereco(endereco.getId());

        verify(enderecoRepository).delete(endereco);
    }

    @Test
    @DisplayName("Não deve remover nenhum endereço.")
    void deleteEnderecoNotFound(){
        UUID endereco_id = UUID.randomUUID();

        when(enderecoRepository.findById(endereco_id)).thenReturn(Optional.empty());

        Exception exception = assertThrows(EnderecoNotFoundException.class, () -> enderecoService.deleteEndereco(endereco_id));

        assertEquals("Endereço não encontrado.", exception.getMessage());
    }

    UsuarioModel gerarUsuario() {
        return new UsuarioModel(UUID.randomUUID(),"carlos@gmail.com","12345", Roles.MEDICO, true, null, null);
    }

    EnderecoModel gerarEndereco() {
        return new EnderecoModel(UUID.randomUUID(), "BA", "Salvador", "41853865", "Brotas", "Rua dos Bobos", "69", null, null);
    }
}