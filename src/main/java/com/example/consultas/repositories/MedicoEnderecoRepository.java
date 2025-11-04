package com.example.consultas.repositories;

import com.example.consultas.models.MedicoEnderecoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MedicoEnderecoRepository extends JpaRepository<MedicoEnderecoModel, UUID> {

    List<MedicoEnderecoModel> findByMedico_Crm(String medicoCrm);

    List<MedicoEnderecoModel> findByMedico_NomeContainingIgnoreCase(String medicoNome);
    
    List<MedicoEnderecoModel> findByCidadeContainingIgnoreCase(String cidade);

    List<MedicoEnderecoModel> findByMedico_Id(UUID medicoId);
}
