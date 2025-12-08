package com.example.consultas.controllers;

import com.example.consultas.dtos.ConsultaDto;
import com.example.consultas.services.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }


    @PostMapping
    public ResponseEntity<EntityModel<ConsultaDto>> addConsulta(@RequestBody @Valid ConsultaDto consultaDto) {
        ConsultaDto consulta = consultaService.addConsulta(consultaDto);
        EntityModel<ConsultaDto> consultaEntity = EntityModel.of(consulta).add(linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).withSelfRel());
        URI local = linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).toUri();

        return ResponseEntity.created(local).body(consultaEntity);
    }


    @GetMapping("/{id}")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<ConsultaDto> getConsulta(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaById(id));
    }


    @GetMapping("/medico/{medico_id}")
    @PreAuthorize("@authz.acessoMedico(#medico_id, authentication)")
    public ResponseEntity<Page<EntityModel<ConsultaDto>>> getConsultaByMedicoId(@PathVariable(value = "medico_id") UUID medico_id, @RequestParam(value = "page", defaultValue = "0") int page,
                                                                   @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultaDto> consultaDtos = consultaService.getConsultaByMedicoId(medico_id, pageable);
        Page<EntityModel<ConsultaDto>> consultaEntities = consultaDtos.map(consulta ->EntityModel.of(consulta)
                .add(linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).withSelfRel()));

        return ResponseEntity.status(HttpStatus.OK).body(consultaEntities);
    }


    @GetMapping("/cliente/{cliente_id}")
    @PreAuthorize("@authz.acessoCliente(#cliente_id, authentication)")
    public ResponseEntity<Page<EntityModel<ConsultaDto>>> getConsultaByClienteId(@PathVariable(value = "cliente_id") UUID cliente_id, @RequestParam(value = "page", defaultValue = "0") int page,
                                                                    @RequestParam(value = "size",defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ConsultaDto> consultaDtos = consultaService.getConsultaByClienteId(cliente_id, pageable);
        Page<EntityModel<ConsultaDto>> consultaEntities = consultaDtos.map(consulta ->EntityModel.of(consulta)
                .add(linkTo(methodOn(ConsultaController.class).getConsulta(consulta.id())).withSelfRel()));

        return ResponseEntity.status(HttpStatus.OK).body(consultaEntities);
    }


    @PutMapping("/{id}")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<EntityModel<ConsultaDto>> updateConsulta(@PathVariable(value = "id") UUID id, @RequestBody @Valid ConsultaDto consultaDto) {
        EntityModel<ConsultaDto> consultaEntity = EntityModel.of(consultaService.updateConsulta(id, consultaDto))
                .add(linkTo(methodOn(ConsultaController.class).getConsulta(id)).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(consultaEntity);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<Void> deleteConsulta(@PathVariable UUID id) {
        consultaService.deleteConsulta(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
