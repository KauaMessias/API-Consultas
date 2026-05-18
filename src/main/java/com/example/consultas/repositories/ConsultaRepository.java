package com.example.consultas.repositories;

import com.example.consultas.dtos.consulta.ConsultaResponseDto;
import com.example.consultas.models.ConsultaModel;
import com.example.consultas.models.Status;
import com.example.consultas.models.UsuarioModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaModel, UUID> {

    List<ConsultaModel> findByCliente_Cpf(String clienteCpf);

    List<ConsultaModel> findByMedico_Crm(String medicoCrm);

    Page<ConsultaModel> findByCliente_Id(UUID clienteId, Pageable pageable);

    Page<ConsultaModel> findByMedico_Id(UUID medicoId, Pageable pageable);

    List<ConsultaModel> findByMedico_NomeContainingIgnoreCaseAndCliente_Cpf(String medicoNome, String clienteCpf);

    List<ConsultaModel> findByCliente_NomeContainingIgnoreCaseAndMedico_Crm(String clienteNome, String medicoCrm);

    List<ConsultaModel> findByCliente_CpfAndMedico_Crm(String clienteCpf, String medicoCrm);

    List<ConsultaModel> findByCliente_CpfAndDataConsultaBetween(String clienteCpf, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    List<ConsultaModel> findByMedico_CrmAndDataConsultaBetween(String medicoCrm, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    List<ConsultaModel> findByMedico_IdAndStatusNot(UUID medicoId, Status status);

    List<ConsultaModel> findByCliente_IdAndStatusNot(UUID clienteId, Status status);


    boolean existsByCliente_IdAndDataConsultaBetween(UUID clienteId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    boolean existsByMedico_IdAndDataConsultaBetween(UUID medicoId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    boolean existsByCliente_IdAndDataConsultaBetweenAndIdNot(UUID clienteId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd, UUID consultaId);

    boolean existsByMedico_IdAndDataConsultaBetweenAndIdNot(UUID medicoId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd, UUID consultaId);

    boolean existsByMedico_Usuario_IdAndCliente_Id(UUID UsuarioId, UUID clienteId);

    boolean existsByCliente_Usuario_Id(UUID clienteUsuarioId);

    boolean existsByIdAndMedico_Usuario_Id(UUID id, UUID medicoUsuarioId);

    boolean existsByIdAndCliente_Usuario(UUID id, UsuarioModel clienteUsuario);

    boolean existsByIdAndCliente_Usuario_Id(UUID id, UUID clienteUsuarioId);

    void deleteByMedico_Id(UUID medicoId);

    void deleteByCliente_Id(UUID clienteId);

    @Query("select c from ConsultaModel c where c.medico.usuario.id = :medicoUsuarioId order by c.status, c.dataConsulta")
    Page<ConsultaModel> findByMedico_UsuarioId(UUID medicoUsuarioId, Pageable pageable);

    @Query("select c from ConsultaModel c where c.cliente.usuario.id = :clienteUsuarioId order by c.status, c.dataConsulta")
    Page<ConsultaModel> findByCliente_UsuarioId(UUID clienteUsuarioId, Pageable pageable);

    Page<ConsultaModel> findByMedico_UsuarioIdOrderByStatus(UUID medicoUsuarioId, Pageable pageable);

    Page<ConsultaModel> findByCliente_UsuarioIdOrderByStatus(UUID clienteUsuarioId, Pageable pageable);

    Page<ConsultaModel> findByCliente_IdOrderByStatus(UUID clienteId, Pageable pageable);

    Page<ConsultaModel> findByMedico_IdOrderByStatus(UUID medicoId, Pageable pageable);

    boolean existsByMedico_IdAndDataConsulta(UUID medicoId, LocalDateTime dataConsulta);


    boolean existsByCliente_IdAndDataConsultaBetweenAndIdNotAndStatusNot(UUID clienteId, LocalDateTime dataConsultaAfter, LocalDateTime dataConsultaBefore, UUID id, Status status);

    boolean existsByMedico_IdAndDataConsultaBetweenAndIdNotAndStatusNot(UUID medicoId, LocalDateTime dataConsultaAfter, LocalDateTime dataConsultaBefore, UUID id, Status status);

    boolean existsByMedico_IdAndDataConsultaBetweenAndStatusNot(UUID medicoId, LocalDateTime dataConsultaAfter, LocalDateTime dataConsultaBefore, Status status);

    boolean existsByCliente_IdAndDataConsultaBetweenAndStatusNot(UUID clienteId, LocalDateTime dataConsultaAfter, LocalDateTime dataConsultaBefore, Status status);
}
