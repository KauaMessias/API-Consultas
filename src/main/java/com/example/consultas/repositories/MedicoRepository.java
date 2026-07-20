package com.example.consultas.repositories;

import com.example.consultas.models.MedicoModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicoRepository extends JpaRepository<MedicoModel, UUID> {

    Optional<MedicoModel> findByCrm(String crm);

    Page<MedicoModel> findByNomeContainingIgnoreCase(Pageable pageable, String nome);

    boolean existsByIdAndUsuario_Id(UUID id, UUID usuarioId);
    
    boolean existsByCrm(String crm);

    boolean existsByUsuario_Id(UUID usuarioId);

    Optional<MedicoModel> findByUsuario_Id(UUID usuarioId);

    @EntityGraph(attributePaths = {"usuario"})
    @Query("select m from MedicoModel m left join m.usuario.enderecos e on e.principal = true" +
            " where (:especialidade is null or m.especialidade like :especialidade) and (:cidade is null or e.cidade like :cidade) and m.usuario.enderecos is not empty")
    Page<MedicoModel> findAll(@Param("especialidade") String especialidade, @Param("cidade") String cidade, Pageable pageable);

}
