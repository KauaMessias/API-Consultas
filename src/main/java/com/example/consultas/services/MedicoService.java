package com.example.consultas.services;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.dtos.medico.HorarioDisponivelDto;
import com.example.consultas.dtos.medico.HorarioDto;
import com.example.consultas.dtos.medico.MedicoRequestDto;
import com.example.consultas.dtos.medico.MedicoResponseDto;
import com.example.consultas.exceptions.*;
import com.example.consultas.models.*;
import com.example.consultas.models.MedicoModel;
import com.example.consultas.repositories.*;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;


import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MedicoService {

    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final ConsultaRepository consultaRepository;
    private final EnderecoRepository enderecoRepository;
    private final HorarioRepository horarioRepository;


    public MedicoService(MedicoRepository medicoRepository, PasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository, ConsultaRepository consultaRepository, EnderecoRepository enderecoRepository, HorarioRepository horarioRepository) {
        this.medicoRepository = medicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
        this.consultaRepository = consultaRepository;
        this.enderecoRepository = enderecoRepository;
        this.horarioRepository = horarioRepository;
    }


    public MedicoResponseDto getMedicoById(UUID id) {
        log.info("Buscando médico com o id {}", id);
        MedicoModel medico = medicoRepository.findById(id).orElseThrow(() -> {
            log.warn("Médico com o id {} não encontrado", id);
            return new MedicoNotFoundException();
        });

        EnderecoModel endereco = medico.getUsuario().getEnderecos().stream().filter(EnderecoModel::getPrincipal).findFirst().orElseThrow(EnderecoNotFoundException::new);

        MedicoResponseDto response = new MedicoResponseDto(medico, new EnderecoDto(endereco));

        return response;
    }


    public MedicoResponseDto getMedicoByCrm(String crm) {
        log.info("Buscando médico com o CRM {}", crm);
        return new MedicoResponseDto(medicoRepository.findByCrm(crm).orElseThrow(() -> {
            log.warn("Médico com o CRM {} não encontrado", crm);
            return new MedicoNotFoundException();
        }));
    }

    public Page<MedicoResponseDto> getMedicoByNome(Pageable pageable, String nome) {
        log.info("Buscando médicos com o nome {}", nome);

        return medicoRepository.findByNomeContainingIgnoreCase(pageable, nome).map(MedicoResponseDto::new);
    }


    public Page<MedicoResponseDto> getAllMedicos(Pageable pageable, String especialidade, String cidade) {
        log.info("Buscando todos os médicos");
        Page<MedicoResponseDto> response = medicoRepository.findAll(pageable, especialidade, cidade).map(m -> {
            EnderecoModel endereco = m.getUsuario().getEnderecos().stream().filter(EnderecoModel::getPrincipal).findFirst().orElse(null);

            if (endereco != null) {
                return new MedicoResponseDto(m, new EnderecoDto(endereco));
            }
            return new MedicoResponseDto(m);
        });
        return response;
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

    public MedicoResponseDto exibirPerfil(UsuarioModel usuarioModel) {
        if (!usuarioModel.getRole().equals(Roles.MEDICO)) {
            throw new ResourceAccessException("Acesso negado");
        }
        return new MedicoResponseDto(medicoRepository.findByUsuario_Id(usuarioModel.getId()).orElseThrow(MedicoNotFoundException::new));
    }

    public HorarioDto addHorario(UsuarioModel usuarioModel, HorarioDto horarioDto) {
        log.info("Adicionando um horário para o usuário com id {}", usuarioModel.getId());
        MedicoModel medico = medicoRepository.findByUsuario_Id(usuarioModel.getId()).orElseThrow(MedicoNotFoundException::new);

        if (horarioDto.horarioInicio().isAfter(horarioDto.horarioFinal())) {
            throw new ConflitoHorarioException();
        }
        HorarioMedico horarioMedico = horarioDto.toEntity();
        horarioMedico.setMedico(medico);

        horarioMedico = horarioRepository.save(horarioMedico);

        log.info("Adicionado com sucesso o horário com id {}", horarioMedico.getId());
        return new HorarioDto(horarioMedico);
    }

    public List<HorarioDto> getAllHorarios(UUID id) {
        log.info("Buscando os horários do médico com id {}", id);
        if (!medicoRepository.existsById(id)) {
            throw new MedicoNotFoundException();
        }
        return horarioRepository.findByMedico_Id(id).stream().map(HorarioDto::new).toList();
    }

    public List<HorarioDisponivelDto> getHorariosDisponiveis(UUID id, LocalDate data) {
        log.info("Buscando os horários disponiveis para o médico com id {}", id);
        if (!medicoRepository.existsById(id)) {
            throw new MedicoNotFoundException();
        }

        List<HorarioMedico> horarios = horarioRepository.findByDiaSemanaAndMedico_Id(DiaSemana.from(data.getDayOfWeek()), id);

        List<HorarioDisponivelDto> horariosDisponiveis = horarios.stream().filter(HorarioMedico::isAtivo)
                .flatMap(horario -> {
                    List<LocalTime> horas = new ArrayList<>();
                    LocalTime horaInicio = horario.getHorarioInicio();
                    while (horaInicio.isBefore(horario.getHorarioFinal())) {
                        horas.add(horaInicio);
                        horaInicio = horaInicio.plusMinutes(horario.getDuracao());
                    }
                    return horas.stream().map(hora -> new HorarioDisponivelDto(data, hora, horario.getId()));
                }).toList();
        log.info("{} horários disponiveis encontrados", horariosDisponiveis.size());
        return horariosDisponiveis;
    }

    public void deletarHorario(UUID id) {
        log.info("Deletando o horário com id {}", id);
        HorarioMedico horario = horarioRepository.findById(id).orElseThrow(HorarioNotFoundException::new);
        horarioRepository.delete(horario);
        log.info("Horário deletado com sucesso.");
    }

    public HorarioDto mudarStatusHorario(UUID id) {
        log.info("Mudando status do horário com id {}", id);
        HorarioMedico horario = horarioRepository.findById(id).orElseThrow(HorarioNotFoundException::new);

        if (horario.isAtivo()) {
            horario.setAtivo(false);
            return new HorarioDto(horarioRepository.save(horario));
        }

        horario.setAtivo(true);
        return new HorarioDto(horarioRepository.save(horario));
    }

}
