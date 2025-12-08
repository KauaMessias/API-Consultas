package com.example.consultas.services;

import com.example.consultas.dtos.ConsultaDto;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.ConflitoConsultaException;
import com.example.consultas.exceptions.ConsultaNotFoundException;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.MedicoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final ClienteRepository clienteRepository;

    public ConsultaService(ConsultaRepository consultaRepository, MedicoRepository medicoRepository, ClienteRepository clienteRepository) {
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
        this.clienteRepository = clienteRepository;
    }


    public List<ConsultaDto> getConsultaByMedicoCrm(String medicoCrm) {
        return consultaRepository.findByMedico_Crm(medicoCrm)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByClienteCpf(String clienteCpf) {
        return consultaRepository.findByCliente_Cpf(clienteCpf)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByMed_NomeAndCli_Cpf(String med_Nome, String clienteCpf) {
        return consultaRepository.findByMedico_NomeContainingIgnoreCaseAndCliente_Cpf(med_Nome, clienteCpf)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }

    public Page<ConsultaDto> getConsultaByMedicoId(UUID id, Pageable pageable) {
        return consultaRepository.findByMedico_Id(id, pageable)
                .map(ConsultaDto::new);
    }


    public Page<ConsultaDto> getConsultaByClienteId(UUID id, Pageable pageable) {
        return consultaRepository.findByCliente_Id(id, pageable)
                .map(ConsultaDto::new);
    }


    public ConsultaDto getConsultaById(UUID id) {
        return new ConsultaDto(consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new));
    }


    @Transactional
    public ConsultaDto addConsulta(ConsultaDto consultaDto) {
        ConsultaModel consultaModel = consultaDto.toEntity();

        MedicoModel medicoModel = medicoRepository.findById(consultaDto.medico_id()).orElseThrow(MedicoNotFoundException::new);
        consultaModel.setMedico(medicoModel);

        ClienteModel clienteModel = clienteRepository.findById(consultaDto.cliente_id()).orElseThrow(ClienteNotFoundException::new);
        consultaModel.setCliente(clienteModel);

        if(consultaRepository.existsByMedico_IdAndDataConsultaBetween(consultaDto.medico_id(), consultaDto.dataConsulta().minusMinutes(15), consultaDto.dataConsulta().plusMinutes(14))) {
            throw new ConflitoConsultaException("Médico já possui uma consulta no horário.");
        }

        if(consultaRepository.existsByCliente_IdAndDataConsultaBetween(consultaDto.cliente_id(), consultaDto.dataConsulta().minusMinutes(15), consultaDto.dataConsulta().plusMinutes(14))){
            throw new ConflitoConsultaException("Cliente já possui uma consulta no horário.");
        }

        return new ConsultaDto(consultaRepository.save(consultaModel));
    }


    @Transactional
    public ConsultaDto updateConsulta(UUID id, ConsultaDto consultaDto) {
        ConsultaModel consultaModel = consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new);

        if(consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(consultaModel.getMedico().getId(), consultaDto.dataConsulta().minusMinutes(15), consultaDto.dataConsulta().plusMinutes(14), consultaModel.getId())) {
            throw new ConflitoConsultaException();
        }

        if(consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(consultaModel.getCliente().getId(), consultaDto.dataConsulta().minusMinutes(15), consultaDto.dataConsulta().plusMinutes(14), consultaModel.getId())){
            throw new ConflitoConsultaException();
        }

        consultaDto.updateEntity(consultaModel);

        return new ConsultaDto(consultaRepository.save(consultaModel));
    }


    @Transactional
    public void deleteConsulta(UUID id) {
        consultaRepository.delete(consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new));
    }

}
