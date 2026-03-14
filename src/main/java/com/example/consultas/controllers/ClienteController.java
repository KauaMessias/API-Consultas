package com.example.consultas.controllers;

import com.example.consultas.dtos.cliente.ClienteRequestDto;
import com.example.consultas.dtos.cliente.ClienteResponseDto;
import com.example.consultas.models.Roles;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.security.SecurityConfigurations;
import com.example.consultas.services.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/clientes")
@Tag(name = "Clientes", description = "Controller para gerenciamento de clientes")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }


    @PostMapping
    @Operation(summary = "Criar um cliente e um usuário associado", description = "método para criar um cliente e usuário associado a ele usando os dados vindos de uma requisição.")
    @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Email ou CPF já cadastrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<EntityModel<ClienteResponseDto>> addCliente(@RequestBody @Valid ClienteRequestDto clienteRequestDto) {
        ClienteResponseDto clienteResponseDto = clienteService.addCliente(clienteRequestDto);
        EntityModel<ClienteResponseDto> clienteDtoEntity = EntityModel.of(clienteResponseDto);
        clienteDtoEntity.add(linkTo(methodOn(ClienteController.class).getClienteById(clienteResponseDto.id())).withSelfRel());

        URI location = linkTo(methodOn(ClienteController.class).getClienteById(clienteResponseDto.id())).toUri();
        return ResponseEntity.created(location).body(clienteDtoEntity);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Buscar um cliente cadastrado", description = "método para buscar os dados de um cliente a partir de seu id")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoCliente(#id, authentication)")
    public ResponseEntity<EntityModel<ClienteResponseDto>> getClienteById(@PathVariable(value = "id") UUID id) {
        EntityModel<ClienteResponseDto> clienteDtoEntity = EntityModel.of(clienteService.getClienteById(id));
        clienteDtoEntity.add(linkTo(methodOn(ClienteController.class).getAllClientes(0, 10)).withRel("all-clientes"));

        return ResponseEntity.status(HttpStatus.OK).body(clienteDtoEntity);
    }


    @GetMapping
    @Operation(summary = "Buscar todos os clientes cadastrados", description = "método para buscar todos os clientes.")
    @ApiResponse(responseCode = "200", description = "Clientes retornados com sucesso")
    @ApiResponse(responseCode = "400", description = "Erro ao buscar clientes")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
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
    @Operation(summary = "Atualizar os dados de um cliente cadastrado", description = "método para atualizar os dados de um cliente a partir de seu id usando os dados vindos de uma requisição.")
    @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoCliente(#id, authentication)")
    public ResponseEntity<EntityModel<ClienteResponseDto>> updateCliente(@PathVariable(value = "id") UUID id, @RequestBody @Valid ClienteRequestDto clienteRequestDto) {
        EntityModel<ClienteResponseDto> clienteDtoEntity = EntityModel.of(clienteService.updateCliente(id, clienteRequestDto));
        clienteDtoEntity.add(
                linkTo(methodOn(ClienteController.class)
                        .getClienteById(id))
                        .withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(clienteDtoEntity);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar cliente", description = "Método para desativar um cliente a partir de seu id")
    @ApiResponse(responseCode = "204", description = "Cliente desativado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoCliente(#id, authentication)")
    public ResponseEntity<Void> desativarCliente(@PathVariable(value = "id") UUID id) {
        clienteService.desativarCliente(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @GetMapping("/perfil")
    public ResponseEntity<ClienteResponseDto> meuPerfil(@AuthenticationPrincipal UsuarioModel usuarioModel){
        ClienteResponseDto response = clienteService.exibirPerfil(usuarioModel);
        return ResponseEntity.ok(response);
    }
}

