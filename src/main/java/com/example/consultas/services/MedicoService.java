package com.example.consultas.services;

import com.example.consultas.dtos.MedicoRequestDto;
import com.example.consultas.dtos.MedicoResponseDto;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.MedicoRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;

    public MedicoService(MedicoRepository medicoRepository, PasswordEncoder passwordEncoder) {
        this.medicoRepository = medicoRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public MedicoResponseDto getMedicoById(UUID id) {
        return new MedicoResponseDto(medicoRepository.findById(id).orElseThrow(MedicoNotFoundException::new));
    }


    public MedicoResponseDto getMedicoByCrm(String crm) {
        return new MedicoResponseDto(medicoRepository.findByCrm(crm).orElseThrow(MedicoNotFoundException::new));
    }


    public MedicoResponseDto getMedicoByEmail(String email) {
        return new MedicoResponseDto(medicoRepository.findByEmail(email).orElseThrow(MedicoNotFoundException::new));
    }


    public List<MedicoResponseDto> getMedicoByNome(String nome) {
        return medicoRepository.findByNomeContainingIgnoreCase(nome)
                .stream().map(MedicoResponseDto::new)
                .toList();
    }


    public Page<MedicoResponseDto> getAllMedicos(Pageable pageable) {
        return medicoRepository.findAll(pageable).map(MedicoResponseDto::new);

    }


    @Transactional
    public void deleteMedico(UUID id) {
        medicoRepository.delete(medicoRepository.findById(id).orElseThrow(MedicoNotFoundException::new));
    }


    @Transactional
    public MedicoResponseDto addMedico(MedicoRequestDto medicoRequestDto) {
        if(medicoRepository.existsByEmail(medicoRequestDto.email())) {
            throw new EntityExistsException("Email já cadastrado.");
        }

        if(medicoRepository.existsByCrm(medicoRequestDto.crm())) {
            throw new EntityExistsException("CRM já cadastrado.");
        }

        MedicoModel medicoModel = new MedicoModel();
        BeanUtils.copyProperties(medicoRequestDto, medicoModel);

        medicoModel.setSenha(passwordEncoder.encode(medicoRequestDto.senha()));

        return new MedicoResponseDto(medicoRepository.save(medicoModel));
    }


    @Transactional
    public MedicoResponseDto updateMedico(UUID id, MedicoRequestDto medicoRequestDto) {
        medicoRepository.findByEmail(medicoRequestDto.email()).ifPresent(medico -> {
            if (!medico.getId().equals(id)) {
                throw new EntityExistsException("Email já cadastrado.");
            }
        });

        medicoRepository.findByCrm(medicoRequestDto.crm()).ifPresent(medico -> {
            if (!medico.getId().equals(id)) {
                throw new EntityExistsException("CRM já cadastrado.");
            }
        });

        MedicoModel medicoModel = medicoRepository.findById(id).orElseThrow(MedicoNotFoundException::new);
        BeanUtils.copyProperties(medicoRequestDto, medicoModel);

        if(medicoRequestDto.senha() != null && !medicoRequestDto.senha().trim().isEmpty()){
            medicoModel.setSenha(passwordEncoder.encode(medicoRequestDto.senha()));
        }

        return new MedicoResponseDto(medicoRepository.save(medicoModel));
    }

}
