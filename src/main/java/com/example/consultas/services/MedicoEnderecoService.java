package com.example.consultas.services;

import com.example.consultas.dtos.MedicoEnderecoDto;
import com.example.consultas.exceptions.MedicoEnderecoNotFoundException;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.MedicoEnderecoModel;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.MedicoEnderecoRepository;
import com.example.consultas.repositories.MedicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class MedicoEnderecoService {

    private final MedicoEnderecoRepository medicoEnderecoRepository;
    private final MedicoRepository medicoRepository;

    public MedicoEnderecoService(MedicoEnderecoRepository medicoEnderecoRepository, MedicoRepository medicoRepository) {
        this.medicoEnderecoRepository = medicoEnderecoRepository;
        this.medicoRepository = medicoRepository;
    }


    public MedicoEnderecoDto getMedicoEnderecoById(UUID id) {
        return new MedicoEnderecoDto(medicoEnderecoRepository.findById(id).orElseThrow(MedicoEnderecoNotFoundException::new));
    }


    public List<MedicoEnderecoDto> getMedicoEnderecoByMedicoId(UUID medicoid) {
        return medicoEnderecoRepository.findByMedico_Id(medicoid).stream().map(MedicoEnderecoDto::new).toList();
    }


    public List<MedicoEnderecoDto> getMedicoEnderecoByCrm(String medicoCrm) {
        return medicoEnderecoRepository.findByMedico_Crm(medicoCrm).stream().map(MedicoEnderecoDto::new).toList();
    }


    public List<MedicoEnderecoDto> getMedicoEnderecoByCidade(String cidade) {
        return medicoEnderecoRepository.findByCidadeContainingIgnoreCase(cidade).stream().map(MedicoEnderecoDto::new).toList();
    }


    public List<MedicoEnderecoDto> getMedicoEnderecoByNome(String nome) {
        return medicoEnderecoRepository.findByMedico_NomeContainingIgnoreCase(nome).stream().map(MedicoEnderecoDto::new).toList();
    }


    @Transactional
    public MedicoEnderecoDto addMedicoEndereco(MedicoEnderecoDto medicoEnderecoDto) {
        MedicoEnderecoModel medicoEnderecoModel = new MedicoEnderecoModel();

        MedicoModel medicoModel = medicoRepository.findById(medicoEnderecoDto.id()).orElseThrow(MedicoNotFoundException::new);
        medicoEnderecoModel.setMedico(medicoModel);

        BeanUtils.copyProperties(medicoEnderecoDto, medicoEnderecoModel);

        return new MedicoEnderecoDto(medicoEnderecoRepository.save(medicoEnderecoModel));
    }


    @Transactional
    public MedicoEnderecoDto updateMedicoEndereco(UUID id, MedicoEnderecoDto medicoEnderecoDto) {
        MedicoEnderecoModel medicoEnderecoModel = medicoEnderecoRepository.findById(id).orElseThrow(MedicoEnderecoNotFoundException::new);

        MedicoModel medicoModel = medicoRepository.findById(medicoEnderecoDto.id()).orElseThrow(MedicoNotFoundException::new);
        medicoEnderecoModel.setMedico(medicoModel);

        BeanUtils.copyProperties(medicoEnderecoDto, medicoEnderecoModel);

        return new MedicoEnderecoDto(medicoEnderecoRepository.save(medicoEnderecoModel));
    }


    @Transactional
    public void deleteMedicoEndereco(UUID id) {
        medicoEnderecoRepository.deleteById(medicoEnderecoRepository.findById(id).orElseThrow(MedicoEnderecoNotFoundException::new).getId());
    }
}
