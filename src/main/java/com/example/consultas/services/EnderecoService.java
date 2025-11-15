package com.example.consultas.services;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.exceptions.EnderecoNotFoundException;
import com.example.consultas.exceptions.UsuarioNotFoundException;
import com.example.consultas.models.EnderecoModel;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.EnderecoRepository;
import com.example.consultas.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;

    public EnderecoService(EnderecoRepository enderecoRepository, UsuarioRepository usuarioRepository) {
        this.enderecoRepository = enderecoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EnderecoDto addEndereco(EnderecoDto enderecoDto, UUID usuario_id) {

        UsuarioModel usuario = usuarioRepository.findById(usuario_id).orElseThrow(UsuarioNotFoundException::new);

        EnderecoModel endereco = new EnderecoModel();
        BeanUtils.copyProperties(enderecoDto, endereco, "id", "usuario");

        endereco.setUsuario(usuario);

        return new EnderecoDto(enderecoRepository.save(endereco));
    }

    @Transactional
    public EnderecoDto updateEndereco(UUID id, EnderecoDto enderecoDto) {
        EnderecoModel endereco = enderecoRepository.findById(id).orElseThrow(EnderecoNotFoundException::new);

        BeanUtils.copyProperties(enderecoDto, endereco, "id", "usuario");

        return new EnderecoDto(enderecoRepository.save(endereco));
    }


    public EnderecoDto getEndereco(UUID id) {
        return new EnderecoDto(enderecoRepository.findById(id).orElseThrow(EnderecoNotFoundException::new));
    }

    public Page<EnderecoDto> getAllEndereco(UUID usuario_id, Pageable pageable) {
        return enderecoRepository.getAllByUsuario_Id(usuario_id, pageable).map(EnderecoDto::new);
    }

    @Transactional
    public void deleteEndereco(UUID id) {
        EnderecoModel endereco = enderecoRepository.findById(id).orElseThrow(EnderecoNotFoundException::new);
        enderecoRepository.delete(endereco);
    }
}
