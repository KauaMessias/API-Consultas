package com.example.consultas.services;

import com.example.consultas.dtos.*;
import com.example.consultas.dtos.MedicoRequestDto;
import com.example.consultas.dtos.MedicoResponseDto;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.*;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.MedicoRepository;
import com.example.consultas.repositories.UsuarioRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    public MedicoService(MedicoRepository medicoRepository, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, AuthorizationService authorizationService) {
        this.medicoRepository = medicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }


    public MedicoResponseDto getMedicoById(UUID id) {
        return new MedicoResponseDto(medicoRepository.findById(id).orElseThrow(MedicoNotFoundException::new));
    }


    public MedicoResponseDto getMedicoByCrm(String crm) {
        return new MedicoResponseDto(medicoRepository.findByCrm(crm).orElseThrow(MedicoNotFoundException::new));
    }

    public Page<MedicoResponseDto> getMedicoByNome(Pageable pageable, String nome) {
        return medicoRepository.findByNomeContainingIgnoreCase(pageable, nome).map(MedicoResponseDto::new);
    }


    public Page<MedicoResponseDto> getAllMedicos(Pageable pageable) {
        return medicoRepository.findAll(pageable).map(MedicoResponseDto::new);
    }


    @Transactional
    public void deleteMedico(UUID id) {
        MedicoModel medico = medicoRepository.findById(id).orElseThrow(MedicoNotFoundException::new);
        UsuarioModel usuario = medico.getUsuario();

        medicoRepository.delete(medico);
        usuarioRepository.delete(usuario);
    }


    @Transactional
    public MedicoResponseDto addMedico(MedicoRequestDto medicoRequestDto) {

        if (usuarioRepository.existsByEmail(medicoRequestDto.email())) {
            throw new EntityExistsException("Email já cadastrado.");
        }

        if (medicoRepository.existsByCrm(medicoRequestDto.crm())) {
            throw new EntityExistsException("CRM já cadastrado.");
        }

        MedicoModel medicoModel = new MedicoModel();
        BeanUtils.copyProperties(medicoRequestDto, medicoModel, "usuario", "senha", "email");

        UsuarioModel usuario = usuarioRepository.save(new UsuarioModel(medicoRequestDto.email(), passwordEncoder.encode(medicoRequestDto.senha()), Roles.MEDICO));
        medicoModel.setUsuario(usuario);

        return new MedicoResponseDto(medicoRepository.save(medicoModel));
    }


    @Transactional
    public MedicoResponseDto updateMedico(UUID id, MedicoRequestDto medicoRequestDto) {
        MedicoModel medicoModel = medicoRepository.findById(id).orElseThrow(MedicoNotFoundException::new);
        UsuarioModel usuario = medicoModel.getUsuario();

        if (!medicoRequestDto.email().equals(usuario.getEmail())) {
            if (usuarioRepository.existsByEmail(medicoRequestDto.email())) {
                throw new EntityExistsException("Email já cadastrado.");
            }
            usuario.setEmail(medicoRequestDto.email());

        }

        if (medicoRequestDto.senha() != null && !medicoRequestDto.senha().trim().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(medicoRequestDto.senha()));
        }

        BeanUtils.copyProperties(medicoRequestDto, medicoModel, "usuario", "senha", "email", "crm");

        usuarioRepository.save(usuario);

        return new MedicoResponseDto(medicoRepository.save(medicoModel));
    }

}
