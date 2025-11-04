package com.example.consultas.services;

import com.example.consultas.dtos.ConsultaDto;
import com.example.consultas.exceptions.ConsultaNotFoundException;
import com.example.consultas.models.ClienteModel;
import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.ConsultaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;

    public ConsultaService(ConsultaRepository consultaRepository){
        this.consultaRepository = consultaRepository;
    }


    public List<ConsultaDto> getConsultaByMedicoCrm(String medicoCrm){
        return consultaRepository.findByMedico_Crm(medicoCrm)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByClienteCpf(String clienteCpf){
        return consultaRepository.findByCliente_Cpf(clienteCpf)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByMed_NomeAndCli_Cpf(String med_Nome, String clienteCpf){
        return consultaRepository.findByMedico_NomeContainingIgnoreCaseAndCliente_Cpf(med_Nome, clienteCpf)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByCli_NomeAndMed_Crm(String clienteNome, String medicoCrm){
        return consultaRepository.findByCliente_NomeContainingIgnoreCaseAndMedico_Crm(clienteNome, medicoCrm)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByCli_CpfAndMedico_Crm(String clienteCpf, String medicoCrm){
        return consultaRepository.findByCliente_CpfAndMedico_Crm(clienteCpf, medicoCrm)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByCli_CpfAndData(String clienteCpf, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd){
        return consultaRepository.findByCliente_CpfAndDataConsultaBetween(clienteCpf, dataConsultaStart, dataConsultaEnd)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByMed_CrmAndData(String medicoCrm, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd){
        return consultaRepository.findByMedico_CrmAndDataConsultaBetween(medicoCrm, dataConsultaStart, dataConsultaEnd)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByMedicoId(UUID id){
        return consultaRepository.findByMedico_Id(id)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public List<ConsultaDto> getConsultaByClienteId(UUID id){
        return consultaRepository.findByCliente_Id(id)
                .stream()
                .map(ConsultaDto::new)
                .toList();
    }


    public ConsultaDto getConsultaById(UUID id){
        return new ConsultaDto(consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new));
    }


    @Transactional
    public ConsultaDto addConsulta(ConsultaDto consultaDto) {
        ConsultaModel consultaModel = new ConsultaModel();
        BeanUtils.copyProperties(consultaDto, consultaModel, "id", "medico", "cliente");

        MedicoModel medicoModel = new MedicoModel();
        medicoModel.setId(consultaDto.medicoId());
        consultaModel.setMedico(medicoModel);
        ClienteModel clienteModel = new ClienteModel();
        clienteModel.setId(consultaDto.clienteId());
        consultaModel.setCliente(clienteModel);

        return new ConsultaDto(consultaRepository.save(consultaModel));
    }


    @Transactional
    public ConsultaDto updateConsulta(UUID id, ConsultaDto consultaDto) {
        ConsultaModel consultaModel = consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new);
        BeanUtils.copyProperties(consultaDto, consultaModel, "id", "medico", "cliente");

        MedicoModel medicoModel = new MedicoModel();
        medicoModel.setId(consultaDto.medicoId());
        consultaModel.setMedico(medicoModel);
        ClienteModel clienteModel = new ClienteModel();
        clienteModel.setId(consultaDto.clienteId());
        consultaModel.setCliente(clienteModel);

        return new ConsultaDto(consultaRepository.save(consultaModel));
    }


    @Transactional
    public void deleteConsulta(UUID id){
        if(!consultaRepository.existsById(id)){
            throw new ConsultaNotFoundException();
        }
        consultaRepository.deleteById(id);
    }

}
