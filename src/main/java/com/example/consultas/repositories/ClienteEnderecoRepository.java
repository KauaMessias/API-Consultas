package com.example.consultas.repositories;

import com.example.consultas.models.ClienteEnderecoModel;
import com.example.consultas.models.ClienteModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClienteEnderecoRepository extends JpaRepository<ClienteEnderecoModel, UUID> {

    List<ClienteEnderecoModel> findByCliente_Cpf(String clienteCpf);

    List<ClienteEnderecoModel> findByCliente_NomeContainingIgnoreCase(String clienteNome);
    
    List<ClienteEnderecoModel> findByCidadeContainingIgnoreCase(String cidade);

    List<ClienteEnderecoModel> findByCliente_Id(UUID id);
}
