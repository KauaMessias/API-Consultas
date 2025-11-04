package com.example.consultas.repositories;

import com.example.consultas.models.MedicoModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoModel, UUID> {

    Optional<MedicoModel> findByCrm(String crm);

    Optional<MedicoModel> findByEmail(String email);

    List<MedicoModel> findByNomeContainingIgnoreCase(String nome);
}
