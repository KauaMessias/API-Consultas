package com.example.consultas.repositories;

import com.example.consultas.models.ClienteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteModel, UUID> {
    
    Optional<ClienteModel> findByCpf(String cpf);
    
    Optional<ClienteModel> findByEmail(String email);

    List<ClienteModel> findByNomeContainingIgnoreCase(String nome);
}
