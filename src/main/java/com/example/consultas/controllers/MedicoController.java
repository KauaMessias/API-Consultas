package com.example.consultas.controllers;

import com.example.consultas.dtos.medico.HorarioDisponivelDto;
import com.example.consultas.dtos.medico.HorarioDto;
import com.example.consultas.dtos.medico.MedicoRequestDto;
import com.example.consultas.dtos.medico.MedicoResponseDto;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.security.SecurityConfigurations;
import com.example.consultas.security.UserAuthorization;
import com.example.consultas.services.MedicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.Remove;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/medicos")
@Tag(name = "Médicos", description = "Controller para gerenciamento de médicos")
@SecurityRequirement(name = SecurityConfigurations.SECURITY)
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }


    @PostMapping
    @Operation(summary = "Criar um médico", description = "Método para usado para criar um médico e um usuário associado a ele usando dados vindos de uma requisição")
    @ApiResponse(responseCode = "201", description = "Médico criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Email ou CRM já cadastrados")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<EntityModel<MedicoResponseDto>> addMedico(@RequestBody @Valid MedicoRequestDto medicoRequestDto) {

        MedicoResponseDto medicoResponse = medicoService.addMedico(medicoRequestDto);
        EntityModel<MedicoResponseDto> medicoDtoEntity = EntityModel.of(medicoResponse);
        medicoDtoEntity.add(linkTo(methodOn(MedicoController.class).getMedicoById(medicoResponse.id())).withSelfRel());

        URI location = linkTo(methodOn(MedicoController.class).getMedicoById(medicoResponse.id())).toUri();

        return ResponseEntity.created(location).body(medicoDtoEntity);
    }


    @GetMapping("/{id}")
    @Operation(summary = "Buscar médico", description = "Método para buscar um médico a partir de seu id")
    @ApiResponse(responseCode = "200", description = "Médico encontrado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<MedicoResponseDto> getMedicoById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(medicoService.getMedicoById(id));
    }

    @GetMapping
    @Operation(summary = "Buscar médicos cadastrados", description = "Método para buscar todos os médicos cadastrados")
    @ApiResponse(responseCode = "200", description = "Médicos encontrados com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    public ResponseEntity<Page<MedicoResponseDto>> getAllMedicos ( @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
                                                                   @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(30) int size, @RequestParam(required = false) String especialidade, @RequestParam(required = false) String cidade) {

        Pageable pageable = PageRequest.of(page, size);

        Page<MedicoResponseDto> medicoResponseDtos = medicoService.getAllMedicos(especialidade, cidade, pageable);

        return ResponseEntity.status(HttpStatus.OK).body(medicoResponseDtos);
    }


    @PutMapping("/{id}")
    @Operation(summary = "Atualizar médico", description = "Método usado para atualizar os dados de um médico a partir de seu id e usando dados vindos da requisição")
    @ApiResponse(responseCode = "200", description = "Médico atualizado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoMedico(#id, authentication)")
    public ResponseEntity<EntityModel<MedicoResponseDto>> updateMedico(@PathVariable UUID id, @RequestBody @Valid MedicoRequestDto medicoRequestDto) {
        EntityModel<MedicoResponseDto> medicoDtoEntity = EntityModel.of(medicoService.updateMedico(id, medicoRequestDto));
        medicoDtoEntity.add(linkTo(methodOn(MedicoController.class).getMedicoById(id)).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(medicoDtoEntity);
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar médico", description = "Método usado para desativar um médico a partir de seu id")
    @ApiResponse(responseCode = "204", description = "Médico desativado com sucesso")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    @ApiResponse(responseCode = "404", description = "Médico não encontrado")
    @ApiResponse(responseCode = "500", description = "Erro no servidor")
    @PreAuthorize("@authz.acessoMedico(#id, authentication)")
    public ResponseEntity<Void> desativarMedico(@PathVariable UUID id) {
        medicoService.desativarMedico(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/perfil")
    public ResponseEntity<MedicoResponseDto> meuPerfil(@AuthenticationPrincipal UsuarioModel usuarioModel) {
        MedicoResponseDto response = medicoService.exibirPerfil(usuarioModel);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/horarios")
    public ResponseEntity<HorarioDto> criarHorarios(@AuthenticationPrincipal UsuarioModel usuarioModel, @RequestBody @Valid HorarioDto horarioDto) {
        HorarioDto response = medicoService.addHorario(usuarioModel, horarioDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{id}/horarios")
    @PreAuthorize("@authz.acessoMedico(#id, authentication)")
    public ResponseEntity<List<HorarioDto>> getAllHorarios(@PathVariable UUID id) {
        List<HorarioDto> response = medicoService.getAllHorarios(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/horarios/disponiveis")
    public ResponseEntity<List<HorarioDisponivelDto>> getHorariosDisponiveis(@PathVariable UUID id, @RequestParam LocalDate data) {
        return ResponseEntity.ok(medicoService.getHorariosDisponiveis(id, data));
    }


    @PatchMapping("/horarios/{id}")
    @PreAuthorize("@authz.acessoHorario(#id, authentication)")
    public ResponseEntity<HorarioDto> mudarStatusHorario(@PathVariable UUID id) {
        HorarioDto response = medicoService.mudarStatusHorario(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/horarios/{id}")
    @PreAuthorize("@authz.acessoHorario(#id, authentication)")
    public ResponseEntity<Void> removerHorario(@PathVariable UUID id) {
        medicoService.deletarHorario(id);
        return ResponseEntity.noContent().build();
    }


}
