package com.example.consultas.repositories;

import com.example.consultas.models.UsuarioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, UUID> {

    Optional<UserDetails> findByEmail(String email);

    boolean existsByEmail(String email);
}
