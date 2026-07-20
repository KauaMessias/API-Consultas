package com.example.consultas.services;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.exceptions.EnderecoNotFoundException;
import com.example.consultas.exceptions.UsuarioNotFoundException;
import com.example.consultas.models.EnderecoModel;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.repositories.EnderecoRepository;
import com.example.consultas.repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final UsuarioRepository usuarioRepository;


    public EnderecoService(EnderecoRepository enderecoRepository, UsuarioRepository usuarioRepository) {
        this.enderecoRepository = enderecoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public EnderecoDto addEndereco(EnderecoDto enderecoDto, UUID usuario_id) {
        log.info("Criando um novo endereço para o usuário de id {}", usuario_id);
        UsuarioModel usuario = usuarioRepository.findById(usuario_id)
                .orElseThrow(() -> {
                    log.warn("Usuário de id {} não encontrado", usuario_id);
                    return new UsuarioNotFoundException();
                });

        EnderecoModel endereco = enderecoDto.toEntity();

        endereco.setUsuario(usuario);

        if(!enderecoRepository.existsByUsuario_Id(usuario_id)){
            endereco.setPrincipal(true);
        }

        endereco = enderecoRepository.save(endereco);
        log.info("Endereço com o id {} criado com sucesso", endereco.getId());

        return new EnderecoDto(endereco);
    }

    @Transactional
    public EnderecoDto updateEndereco(UUID id, EnderecoDto enderecoDto) {
        log.info("Atualizando o endereço com o id {}", id);

        EnderecoModel endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Endereço com o id {} não encontrado", id);
                    return new EnderecoNotFoundException();
                });

        enderecoDto.updateEntity(endereco);

        return new EnderecoDto(enderecoRepository.save(endereco));
    }


    public EnderecoDto getEndereco(UUID id) {
        log.info("Buscando o endereço de id {}", id);

        EnderecoModel endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Endereço com o id {} não encontrado", id);
                    return new EnderecoNotFoundException();
                });

        log.info("Endereço encontrado com sucesso");
        return new EnderecoDto(endereco);
    }

    public Page<EnderecoDto> getAllEnderecoByUsuarioId(UUID usuario_id, Pageable pageable) {
        log.info("Buscando endereços do usuário com o id {}", usuario_id);

        var enderecos = enderecoRepository.findAllByUsuario_Id(usuario_id, pageable).map(EnderecoDto::new);

        log.info("{} endereços encontrados para o usuário com id {}", enderecos.getTotalElements(), usuario_id);

        return enderecos;
    }

    @Transactional
    public void deleteEndereco(UUID id) {
        log.info("Deletando o endereço com o id {}", id);

        EnderecoModel endereco = enderecoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Endereco com o id {} não encontrado", id);
                    return new EnderecoNotFoundException();
                });
        if(endereco.getPrincipal()){
            List<EnderecoModel> enderecos = enderecoRepository.findAllByUsuario_IdAndPrincipal(endereco.getUsuario().getId(), false);
            if(!enderecos.isEmpty()){
                EnderecoModel novoPrincipal = enderecos.getFirst();
                novoPrincipal.setPrincipal(true);
                enderecoRepository.save(novoPrincipal);
            }
        }
        enderecoRepository.delete(endereco);

        log.info("Endereço deletado com sucesso");
    }

    @Transactional
    public EnderecoDto mudarPrincipal(UUID id, UsuarioModel usuarioModel){
        EnderecoModel principal = enderecoRepository.findByUsuario_IdAndPrincipal(usuarioModel.getId(), true).orElseThrow(EnderecoNotFoundException::new);

        principal.setPrincipal(false);
        enderecoRepository.save(principal);

        EnderecoModel endereco = enderecoRepository.findById(id).orElseThrow(EnderecoNotFoundException::new);
        endereco.setPrincipal(true);
        enderecoRepository.save(endereco);

        return new EnderecoDto(endereco);
    }
}
