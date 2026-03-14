package com.example.consultas.controllers;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.security.SecurityConfigurations;
import com.example.consultas.services.EnderecoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/enderecos")
@Tag(name = "Endereços", description = "Controller usado para gerenciamento de endereços")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;

    }

    @PostMapping
    @Operation(summary = "Criar um novo endereço para um usuário", description = "Método usado para criar um endereço para o usuário logado no sistema.")
    @ApiResponse(responseCode = "201", description = "Endereço criado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<EntityModel<EnderecoDto>> criarEndereco(@RequestBody @Valid EnderecoDto enderecoDto, @AuthenticationPrincipal UsuarioModel usuarioModel) {

        enderecoDto = enderecoService.addEndereco(enderecoDto, usuarioModel.getId());

        EntityModel<EnderecoDto> enderecoEntity = EntityModel.of(enderecoDto)
                .add(linkTo(methodOn(EnderecoController.class)
                        .encontrarEndereco(enderecoDto.id())).withSelfRel());


        URI local = linkTo(methodOn(EnderecoController.class).encontrarEndereco(enderecoDto.id())).toUri();

        return ResponseEntity.created(local).body(enderecoEntity);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar um endereço", description = "Método usado para buscar um endereço através de seu id.")
    @ApiResponse(responseCode = "200", description = "Endereço encontrado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<EnderecoDto> encontrarEndereco(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.ok().body(enderecoService.getEndereco(id));
    }


    @GetMapping
    @Operation(summary = "Buscar os endereços de um usuário", description = "Método usado para buscar todos os endereços cadastrados de um usuário através de seu id.")
    @ApiResponse(responseCode = "200", description = "Endereços encontrados com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<Page<EnderecoDto>> encontrarEnderecosByUsuarioId(@AuthenticationPrincipal UsuarioModel usuarioModel,
                                                                                        @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EnderecoDto> enderecoDtos = enderecoService.getAllEnderecoByUsuarioId(usuarioModel.getId(), pageable);


        return ResponseEntity.ok().body(enderecoDtos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar os dados de um endereço", description = "Método usado para atualizar os dados de um endereço cadastrado através de seu id e de dados vindos da requisição.")
    @ApiResponse(responseCode = "200", description = "Endereço atualizado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoEndereco(#id, authentication)")
    public ResponseEntity<EntityModel<EnderecoDto>> editarEndereco(@PathVariable(value = "id") UUID id, @RequestBody @Valid EnderecoDto enderecoDto) {
        enderecoDto = enderecoService.updateEndereco(id, enderecoDto);

        EntityModel<EnderecoDto> enderecoEntity = EntityModel.of(enderecoDto)
                .add(linkTo(methodOn(EnderecoController.class).encontrarEndereco(id)).withSelfRel());

        return ResponseEntity.ok().body(enderecoEntity);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar um endereço", description = "Método usado para deletar um endereço cadastrado através de seu id.")
    @ApiResponse(responseCode = "204", description = "Endereço deletado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Endereço não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoEndereco(#id, authentication)")
    public ResponseEntity<Void> deletarEndereco(@PathVariable(value = "id") UUID id) {
        enderecoService.deleteEndereco(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EnderecoDto> mudarPrincipal(@PathVariable(value = "id") UUID id, @AuthenticationPrincipal UsuarioModel usuarioModel){
        EnderecoDto response = enderecoService.mudarPrincipal(id, usuarioModel);
        return ResponseEntity.ok(response);
    }
}
