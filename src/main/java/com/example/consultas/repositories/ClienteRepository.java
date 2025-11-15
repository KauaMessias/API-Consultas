package com.example.consultas.repositories;

import com.example.consultas.models.ClienteModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteModel, UUID> {
    
    Optional<ClienteModel> findByCpf(String cpf);

    Page<ClienteModel> findByNomeContainingIgnoreCase(Pageable pageable, String nome);

    boolean existsByIdAndUsuario_Id(UUID id, UUID usuario_id);
    
    boolean existsByCpf(String cpf);


}
