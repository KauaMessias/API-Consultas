package com.example.consultas.repositories;

import com.example.consultas.models.EnderecoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EnderecoRepository extends JpaRepository<EnderecoModel, UUID> {

    boolean existsByIdAndUsuario_Id(UUID id, UUID usuario_id);

    Optional<EnderecoModel> findByIdAndUsuario_Id(UUID id, UUID usuario_id);

    Page<EnderecoModel> findByBairroContainingIgnoreCase(String bairro, Pageable pageable);

    Page<EnderecoModel> findByCidadeContainingIgnoreCase(String cep, Pageable pageable);

    Page<EnderecoModel> getAllByUsuario_Id(UUID usuarioId, Pageable pageable);
}
