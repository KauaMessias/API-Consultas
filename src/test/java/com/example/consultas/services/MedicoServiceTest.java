package com.example.consultas.services;

import com.example.consultas.dtos.medico.MedicoRequestDto;
import com.example.consultas.dtos.medico.MedicoResponseDto;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.exceptions.UsuarioInativoException;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.MedicoRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicoServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    EnderecoRepository enderecoRepository;

    @Mock
    MedicoRepository medicoRepository;

    @Mock
    ConsultaRepository consultaRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    MedicoService medicoService;


    @Test
    @DisplayName("Deve gerar um médico.")
    void addMedico() {
        UsuarioModel usuario = gerarUsuario();
        MedicoModel medico = gerarMedico();
        medico.setUsuario(usuario);
        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");
        String senhaCriptografada = "senha-criptografada";
        ArgumentCaptor<UsuarioModel> usuarioArgument = ArgumentCaptor.forClass(UsuarioModel.class);

        when(usuarioRepository.existsByEmail(usuario.getEmail())).thenReturn(false);
        when(medicoRepository.existsByCrm(medicoDto.crm())).thenReturn(false);
        when(medicoRepository.save(any())).thenReturn(medico);
        when(passwordEncoder.encode(medicoDto.senha())).thenReturn(senhaCriptografada);
        when(usuarioRepository.save(any())).thenReturn(usuario);

        MedicoResponseDto result = medicoService.addMedico(medicoDto);

        verify(usuarioRepository).existsByEmail(usuario.getEmail());
        verify(medicoRepository).existsByCrm(medicoDto.crm());
        verify(medicoRepository).save(any(MedicoModel.class));


        verify(usuarioRepository).save(usuarioArgument.capture());
        UsuarioModel usuarioResult = usuarioArgument.getValue();

        assertNotNull(result);
        assertNotNull(result.id());
        assertNotNull(usuarioResult);
        assertEquals(medicoDto.crm(), result.crm());
        assertEquals(medicoDto.nome(), result.nome());
        assertEquals(medicoDto.telefone(), result.telefone());
        assertEquals(medicoDto.email(), usuarioResult.getEmail());
        assertEquals(senhaCriptografada, usuarioResult.getSenha());
    }

    @Test
    @DisplayName("Não deve gerar um médico ao inserir um email existente.")
    void gerarMedicoEmailExiste() {
        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");

        when(usuarioRepository.existsByEmail(medicoDto.email())).thenReturn(true);

        Exception exception = assertThrows(EntityExistsException.class, () -> medicoService.addMedico(medicoDto));
        assertEquals("Email já cadastrado.", exception.getMessage());

        verify(usuarioRepository).existsByEmail(medicoDto.email());
    }

    @Test
    @DisplayName("Não deve gerar um médico ao inserir um crm existente.")
    void gerarMedicoCrmExiste() {
        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");

        when(usuarioRepository.existsByEmail(medicoDto.email())).thenReturn(false);
        when(medicoRepository.existsByCrm(medicoDto.crm())).thenReturn(true);

        Exception exception = assertThrows(EntityExistsException.class, () -> medicoService.addMedico(medicoDto));
        assertEquals("CRM já cadastrado.", exception.getMessage());

        verify(usuarioRepository).existsByEmail(medicoDto.email());
        verify(medicoRepository).existsByCrm(medicoDto.crm());
    }

    @Test
    @DisplayName("Deve atualizar o médico e o email do usuário relacionado.")
    void updateMedico() {
        MedicoModel medico = gerarMedico();
        UsuarioModel usuario = gerarUsuario();
        medico.setUsuario(usuario);

        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto2@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(usuarioRepository.existsByEmail(medicoDto.email())).thenReturn(false);
        when(medicoRepository.save(medico)).thenReturn(medico);

        MedicoResponseDto result = medicoService.updateMedico(medico.getId(), medicoDto);

        assertNotNull(result);
        assertEquals(medico.getId(), result.id());
        assertEquals(medicoDto.crm(), result.crm());
        assertEquals(medicoDto.especialidade(), result.especialidade());
        assertEquals(medicoDto.nome(), result.nome());
        assertEquals(medicoDto.telefone(), result.telefone());
        assertEquals(medicoDto.email(), result.email());

        verify(medicoRepository).findById(medico.getId());
        verify(usuarioRepository).existsByEmail(medicoDto.email());
        verify(usuarioRepository).save(usuario);
        verify(medicoRepository).save(medico);
    }

    @Test
    @DisplayName("Não deve atualizar o médico se email inserido já existe.")
    void updateMedicoEmailExiste() {
        MedicoModel medico = gerarMedico();
        UsuarioModel usuario = gerarUsuario();
        medico.setUsuario(usuario);

        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto2@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(usuarioRepository.existsByEmail(medicoDto.email())).thenReturn(true);

        Exception exception = assertThrows(EntityExistsException.class, () -> medicoService.updateMedico(medico.getId(), medicoDto));
        assertEquals("Email já cadastrado.", exception.getMessage());

        verify(medicoRepository).findById(medico.getId());
        verify(usuarioRepository).existsByEmail(medicoDto.email());
    }

    @Test
    @DisplayName("Não deve atualizar o médico se ele não existir.")
    void updateMedicoNotFound() {
        UUID medicoId = UUID.randomUUID();
        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(MedicoNotFoundException.class, () -> medicoService.updateMedico(medicoId, medicoDto));
        assertEquals("Medico não encontrado.", exception.getMessage());

        verify(medicoRepository).findById(medicoId);
    }

    @Test
    @DisplayName("Não deve atualizar o médico se ele estiver desativado")
    void updateMedicoDesativado() {
        MedicoModel medico = gerarMedico();
        UsuarioModel usuario = gerarUsuario();
        medico.setUsuario(usuario);
        usuario.setEnabled(false);

        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        Exception exception = assertThrows(UsuarioInativoException.class, () -> medicoService.updateMedico(medico.getId(), medicoDto));
        assertEquals("Usuário inativo", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar o médico e a senha de usuário.")
    void updateMedicoAtualizarSenha() {
        MedicoModel medico = gerarMedico();
        UsuarioModel usuario = gerarUsuario();
        medico.setUsuario(usuario);
        ArgumentCaptor<UsuarioModel> usuarioCaptor = ArgumentCaptor.forClass(UsuarioModel.class);

        String email = usuario.getEmail();
        String senhaCriptografada = "novaSenhaCriptografada";

        MedicoRequestDto medicoDto = new MedicoRequestDto("Roberto", "12345", "roberto@gmail.com", "1234", "(71)99999-9999", "Clinico Geral");

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));
        when(passwordEncoder.encode(medicoDto.senha())).thenReturn(senhaCriptografada);
        when(medicoRepository.save(medico)).thenReturn(medico);

        MedicoResponseDto result = medicoService.updateMedico(medico.getId(), medicoDto);

        verify(usuarioRepository).save(usuarioCaptor.capture());
        UsuarioModel usuarioResult = usuarioCaptor.getValue();

        verify(medicoRepository).findById(medico.getId());
        verify(passwordEncoder).encode(medicoDto.senha());
        verify(usuarioRepository, never()).existsByEmail(medicoDto.email());
        verify(medicoRepository).save(medico);

        assertNotNull(result);
        assertNotNull(result.email());
        assertEquals(medico.getId(), result.id());
        assertEquals(medicoDto.crm(), result.crm());
        assertEquals(medico.getEspecialidade(),  result.especialidade());
        assertEquals(medicoDto.nome(), result.nome());
        assertEquals(medicoDto.telefone(), result.telefone());
        assertEquals(email, result.email());
        assertEquals(senhaCriptografada, usuarioResult.getSenha());
    }

    @Test
    @DisplayName("Deve retornar o médico encontrado.")
    void getMedicoById() {
        MedicoModel medico = gerarMedico();
        UsuarioModel usuario = gerarUsuario();
        medico.setUsuario(usuario);

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        MedicoResponseDto result = medicoService.getMedicoById(medico.getId());

        verify(medicoRepository).findById(medico.getId());
        assertEquals(medico.getId(), result.id());
        assertEquals(medico.getNome(), result.nome());
        assertEquals(medico.getCrm(), result.crm());
        assertEquals(medico.getEspecialidade(), result.especialidade());
        assertEquals(medico.getTelefone(), result.telefone());
        assertEquals(usuario.getEmail(), result.email());
    }

    @Test
    @DisplayName("Não deve retornar nenhum médico.")
    void getMedicoByIdNotFound() {
        UUID medicoId = UUID.randomUUID();

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(MedicoNotFoundException.class, () -> medicoService.getMedicoById(medicoId));
        assertEquals("Medico não encontrado.", exception.getMessage());

        verify(medicoRepository).findById(medicoId);
    }

    @Test
    void getMedicoByNome() {
    }

    @Test
    void getAllMedicos() {
    }

    @Test
    @DisplayName("Deve remover um médico, o usuário relacionado e seus endereços.")
    void desativarMedico() {
        MedicoModel medico = gerarMedico();
        UsuarioModel usuario = gerarUsuario();
        medico.setUsuario(usuario);

        when(medicoRepository.findById(medico.getId())).thenReturn(Optional.of(medico));

        medicoService.desativarMedico(medico.getId());

        verify(medicoRepository).findById(medico.getId());
        assertEquals(false, usuario.getEnabled());
    }

    @Test
    @DisplayName("Não deve remover um médico se ele não existir.")
    void desativarMedicoNotFound() {
        UUID medicoId = UUID.randomUUID();

        when(medicoRepository.findById(medicoId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(MedicoNotFoundException.class, () -> medicoService.desativarMedico(medicoId));
        assertEquals("Medico não encontrado.", exception.getMessage());
    }

    private MedicoModel gerarMedico() {
        return new MedicoModel(UUID.randomUUID(), "Roberto", "12345", "(71)99999-9999", "Clinico Geral", null, null, null);
    }

    private UsuarioModel gerarUsuario() {
        return new UsuarioModel(UUID.randomUUID(), "roberto@gmail.com", "1234", Roles.MEDICO, true, null);
    }
}