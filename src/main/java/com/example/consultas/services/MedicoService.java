package com.example.consultas.services;

import com.example.consultas.dtos.medico.MedicoRequestDto;
import com.example.consultas.dtos.medico.MedicoResponseDto;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.exceptions.UsuarioInativoException;
import com.example.consultas.models.*;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.MedicoRepository;
import com.example.consultas.repositories.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;


    public MedicoService(MedicoRepository medicoRepository, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, ConsultaRepository consultaRepository) {
        this.medicoRepository = medicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
    }


    public MedicoResponseDto getMedicoById(UUID id) {
        log.info("Buscando médico com o id {}", id);
        return new MedicoResponseDto(medicoRepository.findById(id).orElseThrow(() -> {
            log.warn("Médico com o id {} não encontrado", id);
            throw new MedicoNotFoundException();
        }));
    }


    public MedicoResponseDto getMedicoByCrm(String crm) {
        log.info("Buscando médico com o CRM {}", crm);
        return new MedicoResponseDto(medicoRepository.findByCrm(crm).orElseThrow(() -> {
            log.warn("Médico com o CRM {} não encontrado", crm);
            throw new MedicoNotFoundException();
        }));
    }

    public Page<MedicoResponseDto> getMedicoByNome(Pageable pageable, String nome) {
        log.info("Buscando médicos com o nome {}", nome);
        return medicoRepository.findByNomeContainingIgnoreCase(pageable, nome).map(MedicoResponseDto::new);
    }


    public Page<MedicoResponseDto> getAllMedicos(Pageable pageable) {
        log.info("Buscando todos os médicos");
        return medicoRepository.findAll(pageable).map(MedicoResponseDto::new);
    }


    @Transactional
    public void desativarMedico(UUID id) {
        MedicoModel medico = medicoRepository.findById(id).orElseThrow(MedicoNotFoundException::new);
        UsuarioModel usuario = medico.getUsuario();

        log.info("Desativando médico com o id {}", medico.getId());
        if (!usuario.isEnabled()) {
            log.warn("O usuário de id {} já está inativo", usuario.getId());
            throw new UsuarioInativoException();
        }

        var consultas = consultaRepository.findByMedico_IdAndStatusNot(medico.getId(), Status.CANCELADA);

        consultas.forEach(consulta -> consulta.setStatus(Status.CANCELADA));

        usuario.setEnabled(false);

        log.info("Médico desativado com sucesso");
    }


    @Transactional
    public MedicoResponseDto addMedico(MedicoRequestDto medicoRequestDto) {

        validarEmail(medicoRequestDto.email());

        if (medicoRepository.existsByCrm(medicoRequestDto.crm())) {
            log.warn("Médico com o CRM {} já existe", medicoRequestDto.crm());
            throw new EntityExistsException("CRM já cadastrado.");
        }

        log.info("Criando novo médico");

        MedicoModel medicoModel = medicoRequestDto.toEntity();

        UsuarioModel usuario = usuarioRepository.save(new UsuarioModel(medicoRequestDto.email(), passwordEncoder.encode(medicoRequestDto.senha()), Roles.MEDICO, true));
        log.debug("Usuário com o id {} criado", usuario.getId());
        medicoModel.setUsuario(usuario);
        medicoModel = medicoRepository.save(medicoModel);

        log.info("Médico com o id {} criado", medicoModel.getId());
        return new MedicoResponseDto(medicoModel);
    }


    @Transactional
    public MedicoResponseDto updateMedico(UUID id, MedicoRequestDto medicoRequestDto) {
        MedicoModel medicoModel = medicoRepository.findById(id).orElseThrow(() -> {
            log.warn("Médico com o id {} não encontrado", id);
            throw new MedicoNotFoundException();
        });
        UsuarioModel usuario = medicoModel.getUsuario();
        if (!usuario.isEnabled()) {
            throw new UsuarioInativoException();
        }

        log.info("Atualizando o médico");


        if (medicoRequestDto.email() != null && !medicoRequestDto.email().trim().isEmpty() && !medicoRequestDto.email().equals(usuario.getEmail())) {
            validarEmail(medicoRequestDto.email());
            usuario.setEmail(medicoRequestDto.email());
            log.info("Email alterado para o médico com id {}", id);
        }

        if (medicoRequestDto.senha() != null && !medicoRequestDto.senha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(medicoRequestDto.senha()));
            log.info("Senha do médico {} alterada", id);
        }

        usuarioRepository.save(usuario);
        log.info("Médico atualizado com sucesso");

        return new MedicoResponseDto(medicoRepository.save(medicoRequestDto.updateEntity(medicoModel)));
    }

    private void validarEmail(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            log.warn("Email já cadastrado");
            throw new EntityExistsException("Email já cadastrado.");
        }
    }

}
