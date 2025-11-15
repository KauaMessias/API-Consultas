package com.example.consultas.repositories;

import com.example.consultas.models.MedicoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoModel, UUID> {

    Optional<MedicoModel> findByCrm(String crm);


    Page<MedicoModel> findByNomeContainingIgnoreCase(Pageable pageable, String nome);

    boolean existsByIdAndUsuario_Id(UUID id, UUID usuarioId);
    
    boolean existsByCrm(String crm);
}
