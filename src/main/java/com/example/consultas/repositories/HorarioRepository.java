package com.example.consultas.repositories;

import com.example.consultas.dtos.medico.HorarioDto;
import com.example.consultas.models.DiaSemana;
import com.example.consultas.models.HorarioMedico;
import com.example.consultas.models.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioRepository extends JpaRepository<HorarioMedico, UUID> {

    List<HorarioMedico> findByMedico_Id(UUID medicoId);

    List<HorarioMedico> findByDiaSemanaAndMedico_Id(DiaSemana diaSemana, UUID medicoId);

    boolean existsByIdAndMedico_Usuario_Id(UUID id, UUID medicoUsuarioId);
}
