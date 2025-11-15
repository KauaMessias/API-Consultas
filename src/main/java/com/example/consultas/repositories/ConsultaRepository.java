package com.example.consultas.repositories;

import com.example.consultas.models.ConsultaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultaRepository extends JpaRepository<ConsultaModel, UUID> {

    List<ConsultaModel> findByCliente_Cpf(String clienteCpf);

    List<ConsultaModel> findByMedico_Crm(String medicoCrm);

    List<ConsultaModel> findByCliente_Id(UUID clienteId);

    List<ConsultaModel> findByMedico_Id(UUID medicoId);

    List<ConsultaModel> findByMedico_NomeContainingIgnoreCaseAndCliente_Cpf(String medicoNome, String clienteCpf);

    List<ConsultaModel> findByCliente_NomeContainingIgnoreCaseAndMedico_Crm(String clienteNome, String medicoCrm);

    List<ConsultaModel> findByCliente_CpfAndMedico_Crm(String clienteCpf, String medicoCrm);

    List<ConsultaModel> findByCliente_CpfAndDataConsultaBetween(String clienteCpf, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    List<ConsultaModel> findByMedico_CrmAndDataConsultaBetween(String medicoCrm, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    boolean existsByCliente_IdAndDataConsultaBetween(UUID clienteId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    boolean existsByMedico_IdAndDataConsultaBetween(UUID medicoId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd);

    boolean existsByCliente_IdAndDataConsultaBetweenAndIdNot(UUID clienteId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd, UUID consultaId);

    boolean existsByMedico_IdAndDataConsultaBetweenAndIdNot(UUID medicoId, LocalDateTime dataConsultaStart, LocalDateTime dataConsultaEnd, UUID consultaId);
}
