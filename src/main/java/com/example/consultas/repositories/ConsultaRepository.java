package com.example.consultas.repositories;

import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaModel, UUID> {

    Page<ConsultaModel> findByCliente_Id(UUID clienteId, Pageable pageable);

    Page<ConsultaModel> findByMedico_Id(UUID medicoId, Pageable pageable);

    List<ConsultaModel> findByMedico_IdAndStatusNot(UUID medicoId, Status status);

    List<ConsultaModel> findByCliente_IdAndStatusNot(UUID clienteId, Status status);

    boolean existsByCliente_IdAndDataConsultaBetween(UUID clienteId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    boolean existsByMedico_IdAndDataConsultaBetween(UUID medicoId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    boolean existsByCliente_IdAndDataConsultaBetweenAndIdNot(UUID clienteId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd, UUID consultaId);

    boolean existsByMedico_IdAndDataConsultaBetweenAndIdNot(UUID medicoId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd, UUID consultaId);

    boolean existsByMedico_Usuario_IdAndCliente_Id(UUID UsuarioId, UUID clienteId);

    boolean existsByCliente_Usuario_Id(UUID clienteUsuarioId);

    boolean existsByIdAndMedico_Usuario_Id(UUID id, UUID medicoUsuarioId);

    boolean existsByIdAndCliente_Usuario_Id(UUID id, UUID clienteUsuarioId);

    @EntityGraph(attributePaths = {"medico", "cliente"})
    @Query("select c from ConsultaModel c where c.medico.usuario.id = :medicoUsuarioId order by c.status, c.dataConsulta")
    Page<ConsultaModel> findByMedico_UsuarioId(UUID medicoUsuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"cliente", "medico"})
    @Query("select c from ConsultaModel c where c.cliente.usuario.id = :clienteUsuarioId order by c.status, c.dataConsulta")
    Page<ConsultaModel> findByCliente_UsuarioId(UUID clienteUsuarioId, Pageable pageable);

    Page<ConsultaModel> findByMedico_IdOrderByStatus(UUID medicoId, Pageable pageable);

    boolean existsByMedico_IdAndDataConsulta(UUID medicoId, LocalDateTime dataConsulta);

    List<ConsultaModel> findByMedico_IdAndStatus(UUID medicoId, Status status);

    boolean existsByMedico_IdAndDataConsultaAndStatusNot(UUID medicoId, LocalDateTime dataConsulta, Status status);

    boolean existsByCliente_IdAndDataConsultaAndStatusNot(UUID clienteId, LocalDateTime dataConsulta, Status status);

    boolean existsByMedico_IdAndDataConsultaAndIdNotAndStatusNot(UUID medicoId, LocalDateTime dataConsulta, UUID id, Status status);

    boolean existsByCliente_IdAndDataConsultaAndIdNotAndStatusNot(UUID clienteId, LocalDateTime dataConsulta, UUID id, Status status);
}
