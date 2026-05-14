package com.example.consultas.controllers;

import com.example.consultas.dtos.consulta.*;
import com.example.consultas.models.Status;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.security.SecurityConfigurations;
import com.example.consultas.services.ConsultaService;
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
@RequestMapping("/api/v1/consultas")
@Tag(name = "Consultas", description = "Controller usado para o gerenciamento de consultas")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }


    @PostMapping
    @Operation(summary = "Criar consulta", description = "Método usado para criar uma consulta através de dados vindos da requisição")
    @ApiResponse(responseCode = "201", description = "Consulta criada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Cliente ou Médico não encontrado")
    @ApiResponse(responseCode = "409", description = "Conflito no horário da consulta")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<EntityModel<ConsultaResponseDto>> addConsulta(@RequestBody @Valid ConsultaDto consultaDto, @AuthenticationPrincipal UsuarioModel usuario) {
        ConsultaResponseDto consulta = consultaService.addConsulta(consultaDto, usuario);
        EntityModel<ConsultaResponseDto> consultaEntity = EntityModel.of(consulta).add(linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).withSelfRel());
        URI local = linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).toUri();

        return ResponseEntity.created(local).body(consultaEntity);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Buscar consulta", description = "Método usado para buscar uma consulta através de seu id")
    @ApiResponse(responseCode = "200", description = "Consulta encontrada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<ConsultaResponseDto> getConsulta(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaById(id));
    }


    @GetMapping("/medico/{medico_id}")
    @Operation(summary = "Buscar consultas de um médico", description = "Método usado para buscar as consulta de um médico através de seu id")
    @ApiResponse(responseCode = "200", description = "Consultas encontradas com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoMedico(#medico_id, authentication)")
    public ResponseEntity<Page<EntityModel<ConsultaResponseDto>>> getConsultaByMedicoId(@PathVariable(value = "medico_id") UUID medico_id, @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                        @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultaResponseDto> consultaDtos = consultaService.getConsultaByMedicoId(medico_id, pageable);
        Page<EntityModel<ConsultaResponseDto>> consultaEntities = consultaDtos.map(consulta -> EntityModel.of(consulta)
                .add(linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).withSelfRel()));

        return ResponseEntity.status(HttpStatus.OK).body(consultaEntities);
    }


    @GetMapping("/cliente/{cliente_id}")
    @Operation(summary = "Buscar consultas de um cliente", description = "Método usado para buscar as consulta de um cliente através de seu id")
    @ApiResponse(responseCode = "200", description = "Consultas encontradas com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoCliente(#cliente_id, authentication)")
    public ResponseEntity<Page<EntityModel<ConsultaResponseDto>>> getConsultaByClienteId(@PathVariable(value = "cliente_id") UUID cliente_id, @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                         @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultaResponseDto> consultaDtos = consultaService.getConsultaByClienteId(cliente_id, pageable);
        Page<EntityModel<ConsultaResponseDto>> consultaEntities = consultaDtos.map(consulta -> EntityModel.of(consulta)
                .add(linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).withSelfRel()));

        return ResponseEntity.status(HttpStatus.OK).body(consultaEntities);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Atualizar consulta", description = "Método usado para atualizar os dados de uma consulta através de seu id usando dados vindos da requisição")
    @ApiResponse(responseCode = "200", description = "Consulta atualizada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<EntityModel<ConsultaResponseDto>> updateConsulta(@PathVariable(value = "id") UUID id, @RequestBody @Valid ConsultaUpdateDto consultaDto) {
        EntityModel<ConsultaResponseDto> consultaEntity = EntityModel.of(consultaService.updateConsulta(id, consultaDto))
                .add(linkTo(methodOn(ConsultaController.class).getConsulta(id)).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(consultaEntity);
    }


    @PatchMapping("/{id}")
    @Operation(summary = "Alterar status da consulta", description = "Método usado para alterar o status de uma consulta através de seu id")
    @ApiResponse(responseCode = "204", description = "Consulta alterada com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Consulta não encontrada")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<ConsultaResponseDto> alterarStatusConsulta(@PathVariable UUID id, @RequestParam String status) {
        ConsultaResponseDto response = consultaService.alterarStatusConsulta(id, status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("cliente/minhasConsultas")
    public ResponseEntity<Page<ConsultaClienteDto>> consultasCliente(@AuthenticationPrincipal UsuarioModel usuarioModel, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultaClienteDto> response = consultaService.buscarConsultasCliente(usuarioModel, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("medico/minhasConsultas")
    public ResponseEntity<Page<ConsultaMedicoDto>> consultasMedico(@AuthenticationPrincipal UsuarioModel usuarioModel, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultaMedicoDto> response = consultaService.buscarConsultasMedico(usuarioModel, pageable);

        return ResponseEntity.ok(response);
    }


}
