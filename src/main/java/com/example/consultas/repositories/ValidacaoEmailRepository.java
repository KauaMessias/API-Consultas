package com.example.consultas.repositories;

import com.example.consultas.models.UsuarioModel;
import com.example.consultas.models.ValidacaoEmailModel;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface ValidacaoEmailRepository extends JpaRepository<ValidacaoEmailModel, UUID> {

    void deleteByUsuario(UsuarioModel usuario);
}
