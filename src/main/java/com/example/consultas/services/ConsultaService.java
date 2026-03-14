package com.example.consultas.services;

import com.example.consultas.dtos.consulta.*;
import com.example.consultas.exceptions.ClienteNotFoundException;
import com.example.consultas.exceptions.ConflitoConsultaException;
import com.example.consultas.exceptions.ConsultaNotFoundException;
import com.example.consultas.exceptions.MedicoNotFoundException;
import com.example.consultas.models.*;
import com.example.consultas.repositories.ClienteRepository;
import com.example.consultas.repositories.ConsultaRepository;
import com.example.consultas.repositories.MedicoRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final ClienteRepository clienteRepository;
    private final int INTERVALO_CONSULTA = 15;


    public ConsultaService(ConsultaRepository consultaRepository, MedicoRepository medicoRepository, ClienteRepository clienteRepository) {
        this.consultaRepository = consultaRepository;
        this.medicoRepository = medicoRepository;
        this.clienteRepository = clienteRepository;
    }


    public List<ConsultaResponseDto> getConsultaByMedicoCrm(String medicoCrm) {
        log.info("Buscando consultas do médico com o CRM {}", medicoCrm);
        var consultas = consultaRepository.findByMedico_Crm(medicoCrm)
                .stream()
                .map(ConsultaResponseDto::new)
                .toList();

        log.info("{} consultas encontradas", consultas.size());
        return consultas;
    }


    public List<ConsultaResponseDto> getConsultaByClienteCpf(String clienteCpf) {
        log.info("Buscando consultas de um cliente");
        var consultas = consultaRepository.findByCliente_Cpf(clienteCpf)
                .stream()
                .map(ConsultaResponseDto::new)
                .toList();
        log.info("{} consultas encontradas", consultas.size());
        return consultas;
    }


    public Page<ConsultaResponseDto> getConsultaByMedicoId(UUID id, Pageable pageable) {
        log.info("Buscando consultas do médico com id {}", id);
        if (!medicoRepository.existsById(id)) {
            throw new MedicoNotFoundException();
        }
        var consultas = consultaRepository.findByMedico_Id(id, pageable)
                .map(ConsultaResponseDto::new);
        log.info("{} consultas encontradas", consultas.getTotalElements());

        return consultas;
    }


    public Page<ConsultaResponseDto> getConsultaByClienteId(UUID id, Pageable pageable) {
        log.info("Buscando consultas do cliente com id {}", id);
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNotFoundException();
        }
        var consultas = consultaRepository.findByCliente_Id(id, pageable)
                .map(ConsultaResponseDto::new);
        log.info("{} consultas encontradas", consultas.getTotalElements());

        return consultas;
    }


    public ConsultaResponseDto getConsultaById(UUID id) {
        log.info("Buscando consulta com id {}", id);
        var consulta = consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new);
        log.info("Consulta encontrada com sucesso");

        return new ConsultaResponseDto(consulta);
    }


    @Transactional
    public ConsultaResponseDto addConsulta(ConsultaDto consultaDto) {
        log.info("Criando uma nova consulta");
        ConsultaModel consultaModel = consultaDto.toEntity();

        MedicoModel medicoModel = medicoRepository.findById(consultaDto.medico_id()).orElseThrow(MedicoNotFoundException::new);
        consultaModel.setMedico(medicoModel);

        ClienteModel clienteModel = clienteRepository.findById(consultaDto.cliente_id()).orElseThrow(ClienteNotFoundException::new);
        consultaModel.setCliente(clienteModel);

        if (consultaRepository.existsByMedico_IdAndDataConsultaBetween(consultaDto.medico_id(), consultaDto.dataConsulta().minusMinutes(INTERVALO_CONSULTA), consultaDto.dataConsulta().plusMinutes(INTERVALO_CONSULTA))) {
            log.warn("Médico com o id {} já possui uma consulta neste horário", medicoModel.getId());
            throw new ConflitoConsultaException("Médico já possui uma consulta no horário.");
        }

        if (consultaRepository.existsByCliente_IdAndDataConsultaBetween(consultaDto.cliente_id(), consultaDto.dataConsulta().minusMinutes(INTERVALO_CONSULTA), consultaDto.dataConsulta().plusMinutes(INTERVALO_CONSULTA))) {
            log.warn("Cliente com o id {} já possui uma consulta neste horário", clienteModel.getId());
            throw new ConflitoConsultaException("Cliente já possui uma consulta no horário.");
        }

        consultaModel.setStatus(Status.PENDENTE);

        consultaModel = consultaRepository.save(consultaModel);

        log.info("Consulta com o id {} criada com sucesso", consultaModel.getId());

        return new ConsultaResponseDto(consultaModel);
    }


    @Transactional
    public ConsultaResponseDto updateConsulta(UUID id, ConsultaUpdateDto consultaDto) {
        log.info("Atualizando a consulta de id {}", id);
        ConsultaModel consultaModel = consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new);

        verificarStatusConsulta(consultaModel);

        if (consultaRepository.existsByMedico_IdAndDataConsultaBetweenAndIdNot(consultaModel.getMedico().getId(), consultaDto.data().minusMinutes(INTERVALO_CONSULTA), consultaDto.data().plusMinutes(INTERVALO_CONSULTA), consultaModel.getId())) {
            log.warn("Médico com o id {} já possui uma consulta neste horário", consultaModel.getMedico().getId());
            throw new ConflitoConsultaException();
        }

        if (consultaRepository.existsByCliente_IdAndDataConsultaBetweenAndIdNot(consultaModel.getCliente().getId(), consultaDto.data().minusMinutes(INTERVALO_CONSULTA), consultaDto.data().plusMinutes(INTERVALO_CONSULTA), consultaModel.getId())) {
            log.warn("Cliente com o id {} já possui uma consulta neste horário", consultaModel.getCliente().getId());
            throw new ConflitoConsultaException();
        }

        consultaModel = consultaRepository.save(consultaDto.updateConsulta(consultaModel));
        log.info("Consulta atualizada com sucesso");

        return new ConsultaResponseDto(consultaModel);
    }


    @Transactional
    public void alterarStatusConsulta(UUID id, String status) {
        ConsultaModel consultaModel = consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new);

        verificarStatusConsulta(consultaModel);
        consultaModel.setStatus(Status.valueOf(status));
        consultaRepository.save(consultaModel);
    }


    private void verificarStatusConsulta(ConsultaModel consultaModel) {
        if (consultaModel.getStatus().equals(Status.CANCELADA) || consultaModel.getStatus().equals(Status.CONCLUIDA)) {
            log.warn("Consulta {}", consultaModel.getStatus());
            throw new ConflitoConsultaException("Consulta " + consultaModel.getStatus());

        }
    }

    public Page<ConsultaMedicoDto> buscarConsultasMedico(UsuarioModel usuarioModel, Pageable pageable) {
        log.info("Buscando consultas do usuário com id {}", usuarioModel.getId());

        Page<ConsultaMedicoDto> response;

        response = consultaRepository.findByMedico_UsuarioId(usuarioModel.getId(), pageable).map(ConsultaMedicoDto::new);

        log.info("{} consultas encontradas", response.getTotalElements());

        return response;
    }

    public Page<ConsultaClienteDto> buscarConsultasCliente(UsuarioModel usuarioModel, Pageable pageable) {
        log.info("Buscando consultas do usuário com id {}", usuarioModel.getId());

        Page<ConsultaClienteDto> response;

        response = consultaRepository.findByCliente_UsuarioId(usuarioModel.getId(), pageable).map(ConsultaClienteDto::new);

        log.info("{} consultas encontradas", response.getTotalElements());

        return response;
    }
}

