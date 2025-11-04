package com.example.consultas.controllers;

import com.example.consultas.dtos.ClienteRequestDto;
import com.example.consultas.dtos.ClienteResponseDto;
import com.example.consultas.services.ClienteService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/clientes")
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
    public ResponseEntity<EntityModel<ClienteResponseDto>> getClienteById(@PathVariable(value = "id") UUID id) {
        EntityModel<ClienteResponseDto> clienteDtoEntity = EntityModel.of(clienteService.getClienteById(id));
        clienteDtoEntity.add(linkTo(methodOn(ClienteController.class).getAllClientes()).withRel("all-clientes"));

        return ResponseEntity.status(HttpStatus.OK).body(clienteDtoEntity);
    }


    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ClienteResponseDto>>> getAllClientes() {
        List<ClienteResponseDto> clientesDto = clienteService.getAllClientes();

        List<EntityModel<ClienteResponseDto>> clientesDtoEntities = clientesDto
                .stream()
                .map(cliente -> EntityModel.of(cliente).add(linkTo(methodOn(ClienteController.class).getClienteById(cliente.id())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<ClienteResponseDto>> clientesCollectionModel = CollectionModel.of(clientesDtoEntities, linkTo(methodOn(ClienteController.class).getAllClientes()).withSelfRel());
        return ResponseEntity.status(HttpStatus.OK).body(clientesCollectionModel);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ClienteResponseDto>> updateCliente(@PathVariable(value = "id") UUID id, @RequestBody @Valid ClienteRequestDto clienteRequestDto) {
        EntityModel<ClienteResponseDto>  clienteDtoEntity = EntityModel.of(clienteService.updateCliente(id, clienteRequestDto));
        clienteDtoEntity.add(linkTo(methodOn(ClienteController.class).getClienteById(id)).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(clienteDtoEntity);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable(value = "id") UUID id) {
        clienteService.deleteCliente(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

