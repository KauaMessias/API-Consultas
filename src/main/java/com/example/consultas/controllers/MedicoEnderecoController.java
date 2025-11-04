package com.example.consultas.controllers;

import com.example.consultas.dtos.MedicoEnderecoDto;
import com.example.consultas.services.MedicoEnderecoService;
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
@RequestMapping("/enderecosmedico")
public class MedicoEnderecoController {

    private final MedicoEnderecoService medicoEnderecoService;

    public MedicoEnderecoController(MedicoEnderecoService medicoEnderecoService) {
        this.medicoEnderecoService = medicoEnderecoService;
    }


    @PostMapping
    public ResponseEntity<EntityModel<MedicoEnderecoDto>> addEndereco(@RequestBody @Valid MedicoEnderecoDto medicoEnderecoDto) {
        MedicoEnderecoDto enderecoDto = medicoEnderecoService.addMedicoEndereco(medicoEnderecoDto);
        EntityModel<MedicoEnderecoDto> enderecoDtoEntity = EntityModel.of(enderecoDto);
        enderecoDtoEntity.add(linkTo(methodOn(MedicoEnderecoController.class).getMedicoEndereco(enderecoDto.id())).withSelfRel());

        URI location = linkTo(methodOn(MedicoEnderecoController.class).getMedicoEndereco(enderecoDto.id())).toUri();

        return ResponseEntity.created(location).body(enderecoDtoEntity);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<MedicoEnderecoDto>> getMedicoEndereco(@PathVariable(value = "id") UUID id) {
        EntityModel<MedicoEnderecoDto> enderecoDtoEntity = EntityModel.of(medicoEnderecoService.getMedicoEnderecoById(id));

        enderecoDtoEntity.add(linkTo(methodOn(MedicoEnderecoController.class).getMedicoEnderecos(enderecoDtoEntity.getContent().medico_id())).withRel("enderecos"));

        return ResponseEntity.status(HttpStatus.OK).body(enderecoDtoEntity);
    }


    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<CollectionModel<EntityModel<MedicoEnderecoDto>>> getMedicoEnderecos(@PathVariable(value = "medicoId") UUID medicoId) {
        List<EntityModel<MedicoEnderecoDto>> enderecoDtoEntities = medicoEnderecoService.getMedicoEnderecoByMedicoId(medicoId)
                .stream()
                .map(endereco -> EntityModel.of(endereco)
                        .add(linkTo(methodOn(MedicoEnderecoController.class).getMedicoEndereco(endereco.id()))
                                .withSelfRel()))
                .toList();

        CollectionModel<EntityModel<MedicoEnderecoDto>> enderecoDtoCollection = CollectionModel.of(enderecoDtoEntities);

        return ResponseEntity.status(HttpStatus.OK).body(enderecoDtoCollection.add(linkTo(methodOn(MedicoEnderecoController.class).getMedicoEnderecos(medicoId)).withSelfRel()));
    }


    @GetMapping("/crm/{crm}")
    public ResponseEntity<CollectionModel<EntityModel<MedicoEnderecoDto>>> getMedicoEnderecosByCrm(@PathVariable(value = "crm") String crm) {
        List<EntityModel<MedicoEnderecoDto>> enderecoDtoEntities = medicoEnderecoService.getMedicoEnderecoByCrm(crm)
                .stream()
                .map(endereco -> EntityModel.of(endereco)
                        .add(linkTo(methodOn(MedicoEnderecoController.class).getMedicoEndereco(endereco.id()))
                                .withSelfRel()))
                .toList();

        CollectionModel<EntityModel<MedicoEnderecoDto>> enderecoDtoCollection = CollectionModel.of(enderecoDtoEntities);

        return ResponseEntity.status(HttpStatus.OK).body(enderecoDtoCollection.add(linkTo(methodOn(MedicoEnderecoController.class).getMedicoEnderecosByCrm(crm)).withSelfRel()));
    }


    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<MedicoEnderecoDto>> updateMedicoEndereco(@PathVariable(value = "id") UUID id, @RequestBody @Valid MedicoEnderecoDto medicoEnderecoDto) {
        EntityModel<MedicoEnderecoDto> enderecoDtoEntity = EntityModel.of(medicoEnderecoService.updateMedicoEndereco(id, medicoEnderecoDto));
        enderecoDtoEntity.add(linkTo(methodOn(MedicoEnderecoController.class).getMedicoEndereco(id)).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(enderecoDtoEntity);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicoEndereco(@PathVariable(value = "id") UUID id) {
        medicoEnderecoService.deleteMedicoEndereco(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}