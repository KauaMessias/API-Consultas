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

import java.time.LocalDateTime;
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

    public Page<ConsultaResponseDto> getConsultaByMedicoId(UUID id, Pageable pageable) {
        log.info("Buscando consultas do médico com id {}", id);
        if (!medicoRepository.existsById(id)) {
            throw new MedicoNotFoundException();
        }
        var consultas = consultaRepository.findByMedico_IdOrderByStatus(id, pageable)
                .map(ConsultaResponseDto::new);
        log.info("{} consultas encontradas", consultas.getTotalElements());

        return consultas;
    }


    public Page<ConsultaClienteDto> getConsultaByClienteId(UUID id, Pageable pageable) {
        log.info("Buscando consultas do cliente com id {}", id);
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNotFoundException();
        }
        var consultas = consultaRepository.findByCliente_Id(id, pageable)
                .map(ConsultaClienteDto::new);
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
    public ConsultaResponseDto addConsulta(ConsultaDto consultaDto, UsuarioModel usuario) {
        log.info("Criando uma nova consulta");
        ConsultaModel consultaModel = consultaDto.toEntity();

        MedicoModel medicoModel = medicoRepository.findById(consultaDto.medico_id()).orElseThrow(MedicoNotFoundException::new);
        consultaModel.setMedico(medicoModel);

        ClienteModel clienteModel = clienteRepository.findByUsuario_Id(usuario.getId()).orElseThrow(ClienteNotFoundException::new);
        consultaModel.setCliente(clienteModel);

        if (consultaRepository.existsByMedico_IdAndDataConsultaAndStatusNot(consultaDto.medico_id(), consultaDto.dataConsulta(), Status.CANCELADA)) {
            log.warn("Médico com o id {} já possui uma consulta neste horário", medicoModel.getId());
            throw new ConflitoConsultaException("Médico já possui uma consulta no horário.");
        }

        if (consultaRepository.existsByCliente_IdAndDataConsultaAndStatusNot(clienteModel.getId(), consultaDto.dataConsulta(), Status.CANCELADA)) {
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

        if (consultaRepository.existsByMedico_IdAndDataConsultaAndIdNotAndStatusNot(consultaModel.getMedico().getId(), consultaDto.data(), consultaModel.getId(), Status.CANCELADA)) {
            log.warn("Médico com o id {} já possui uma consulta neste horário", consultaModel.getMedico().getId());
            throw new ConflitoConsultaException();
        }

        if (consultaRepository.existsByCliente_IdAndDataConsultaAndIdNotAndStatusNot(consultaModel.getCliente().getId(), consultaDto.data(), consultaModel.getId(), Status.CANCELADA)) {
            log.warn("Cliente com o id {} já possui uma consulta neste horário", consultaModel.getCliente().getId());
            throw new ConflitoConsultaException();
        }

        consultaModel = consultaRepository.save(consultaDto.updateConsulta(consultaModel));
        log.info("Consulta atualizada com sucesso");

        return new ConsultaResponseDto(consultaModel);
    }


    @Transactional
    public ConsultaResponseDto alterarStatusConsulta(UUID id, String status) {
        ConsultaModel consultaModel = consultaRepository.findById(id).orElseThrow(ConsultaNotFoundException::new);
        Status statusNovo;
        try {
            statusNovo = Status.valueOf(status);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ConflitoConsultaException("Status inválido");
        }

        verificarStatusConsulta(consultaModel);

        if (statusNovo.equals(Status.CONCLUIDA) && consultaModel.getDataConsulta().isAfter(LocalDateTime.now()))
            throw new ConflitoConsultaException("Consulta ainda não ocorreu.");

        consultaModel.setStatus(statusNovo);

        return new ConsultaResponseDto(consultaRepository.save(consultaModel));
    }


    private void verificarStatusConsulta(ConsultaModel consultaModel) {
        if (!consultaModel.getStatus().equals(Status.PENDENTE)) {
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

