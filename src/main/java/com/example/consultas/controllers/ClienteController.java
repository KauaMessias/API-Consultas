package com.example.consultas.controllers;

import com.example.consultas.dtos.ClienteRequestDto;
import com.example.consultas.dtos.ClienteResponseDto;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.services.ClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }


    @PostMapping
    public ResponseEntity<EntityModel<ClienteResponseDto>> addCliente(@RequestBody @Valid ClienteRequestDto clienteRequestDto) {
        ClienteResponseDto clienteResponseDto = clienteService.addCliente(clienteRequestDto);
        EntityModel<ClienteResponseDto> clienteDtoEntity = EntityModel.of(clienteResponseDto);
        clienteDtoEntity.add(linkTo(methodOn(ClienteController.class).getClienteById(clienteResponseDto.id())).withSelfRel());

        URI location = linkTo(methodOn(ClienteController.class).getClienteById(clienteResponseDto.id())).toUri();
        return ResponseEntity.created(location).body(clienteDtoEntity);
    }


    @GetMapping("/{id}")
    @PreAuthorize("@authz.autorizado(#id, authentication)")
    public ResponseEntity<EntityModel<ClienteResponseDto>> getClienteById(@PathVariable(value = "id") UUID id) {
        EntityModel<ClienteResponseDto> clienteDtoEntity = EntityModel.of(clienteService.getClienteById(id));
        clienteDtoEntity.add(linkTo(methodOn(ClienteController.class).getAllClientes(0, 10)).withRel("all-clientes"));

        return ResponseEntity.status(HttpStatus.OK).body(clienteDtoEntity);
    }


    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ClienteResponseDto>>> getAllClientes(@RequestParam(defaultValue = "0", value = "page") @Min(0) int page,
                                                                                           @RequestParam(defaultValue = "10", value = "size") @Min(1) @Max(25) int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<ClienteResponseDto> clientesDto = clienteService.getAllClientes(pageable);

        Page<EntityModel<ClienteResponseDto>> clientesDtoEntities = clientesDto
                .map(cliente -> EntityModel.of(cliente)
                        .add(linkTo(methodOn(ClienteController.class)
                                .getClienteById(cliente.id()))
                                .withSelfRel()));

        CollectionModel<EntityModel<ClienteResponseDto>> clientesCollectionModel = CollectionModel.of(clientesDtoEntities, linkTo(methodOn(ClienteController.class).getAllClientes(page, size)).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(clientesCollectionModel);
    }


    @PutMapping("/{id}")
    @PreAuthorize("@authz.autorizado(#id, authentication)")
    public ResponseEntity<EntityModel<ClienteResponseDto>> updateCliente(@PathVariable(value = "id") UUID id, @RequestBody @Valid ClienteRequestDto clienteRequestDto) {
        EntityModel<ClienteResponseDto> clienteDtoEntity = EntityModel.of(clienteService.updateCliente(id, clienteRequestDto));
        clienteDtoEntity.add(
                linkTo(methodOn(ClienteController.class)
                        .getClienteById(id))
                        .withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(clienteDtoEntity);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.autorizado(#id, authentication)")
    public ResponseEntity<Void> deleteCliente(@PathVariable(value = "id") UUID id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

