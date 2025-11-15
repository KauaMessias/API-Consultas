package com.example.consultas.controllers;

import com.example.consultas.dtos.EnderecoDto;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.services.EnderecoService;
import jakarta.validation.Valid;
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
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService){
        this.enderecoService = enderecoService;
    }

    @PostMapping
    public ResponseEntity<EntityModel<EnderecoDto>> criarEndereco(@RequestBody @Valid EnderecoDto enderecoDto, @AuthenticationPrincipal UsuarioModel usuarioModel) {

        enderecoDto = enderecoService.addEndereco(enderecoDto, usuarioModel.getId());

        EntityModel<EnderecoDto> enderecoEntity = EntityModel.of(enderecoDto)
                .add(linkTo(methodOn(EnderecoController.class)
                        .encontrarEndereco(enderecoDto.id())).withSelfRel());


        URI local = linkTo(methodOn(EnderecoController.class).encontrarEndereco(enderecoDto.id())).toUri();

        return ResponseEntity.created(local).body(enderecoEntity);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@enderecoAuthz.autorizado(#id, authentication)")
    public ResponseEntity<EnderecoDto> encontrarEndereco(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.ok().body(enderecoService.getEndereco(id));
    }


    @GetMapping
    public ResponseEntity<Page<EntityModel<EnderecoDto>>> encontrarEnderecos(@AuthenticationPrincipal UsuarioModel usuarioModel, @RequestParam(value = "page",defaultValue = "0") int page,
                                                                       @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EnderecoDto> enderecoDtos = enderecoService.getAllEndereco(usuarioModel.getId(), pageable);

        Page<EntityModel<EnderecoDto>> enderecoEntities = enderecoDtos.map(endereco -> EntityModel.of(endereco)
                .add((linkTo(methodOn(EnderecoController.class).encontrarEndereco(endereco.id())).withSelfRel())));

        return ResponseEntity.ok().body(enderecoEntities);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@enderecoAuthz.autorizado(#id, authentication)")
    public ResponseEntity<EntityModel<EnderecoDto>> editarEndereco(@PathVariable(value = "id") UUID id, @RequestBody @Valid EnderecoDto enderecoDto) {
        enderecoDto = enderecoService.updateEndereco(id, enderecoDto);

        EntityModel<EnderecoDto> enderecoEntity = EntityModel.of(enderecoDto)
                .add(linkTo(methodOn(EnderecoController.class).encontrarEndereco(id)).withSelfRel());

        return ResponseEntity.ok().body(enderecoEntity);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@enderecoAuthz.autorizado(#id, authentication)")
    public ResponseEntity<Void> deletarEndereco(@PathVariable(value = "id") UUID id) {
        enderecoService.deleteEndereco(id);
        return ResponseEntity.noContent().build();
    }
}
