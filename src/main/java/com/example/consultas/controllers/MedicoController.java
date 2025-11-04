package com.example.consultas.controllers;

import com.example.consultas.dtos.MedicoRequestDto;
import com.example.consultas.dtos.MedicoResponseDto;
import com.example.consultas.services.MedicoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }


    @PostMapping
    public ResponseEntity<EntityModel<MedicoResponseDto>> addMedico(@RequestBody @Valid MedicoRequestDto medicoRequestDto) {

        MedicoResponseDto medicoResponse = medicoService.addMedico(medicoRequestDto);
        EntityModel<MedicoResponseDto> medicoDtoEntity = EntityModel.of(medicoResponse);
        medicoDtoEntity.add( linkTo(methodOn(MedicoController.class).getMedicoById(medicoResponse.id())).withSelfRel());

        URI location = linkTo(methodOn(MedicoController.class).getMedicoById(medicoResponse.id())).toUri();

        return ResponseEntity.created(location).body(medicoDtoEntity);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<MedicoResponseDto>> getMedicoById(@PathVariable(value = "id") UUID id) {
        EntityModel<MedicoResponseDto> medicoDtoEntity = EntityModel.of(medicoService.getMedicoById(id));
        medicoDtoEntity.add(linkTo(methodOn(MedicoController.class).getAllMedicos(10, 0)).withRel("all-medicos"));

        return ResponseEntity.ok().body(medicoDtoEntity);
    }


    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MedicoResponseDto>>> getAllMedicos(@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, size);

        List<MedicoResponseDto> medicoResponseDtos = medicoService.getAllMedicos(pageable).toList();

        List<EntityModel<MedicoResponseDto>> medicoEntities = medicoResponseDtos.stream()
                .map(medico -> EntityModel.of(medico)
                        .add(linkTo(methodOn(MedicoController.class).getMedicoById(medico.id())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<MedicoResponseDto>> collectionModel = CollectionModel.of(medicoEntities, linkTo(methodOn(MedicoController.class).getAllMedicos(size, page)).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(collectionModel);
    }


    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<MedicoResponseDto>> updateMedico(@PathVariable(value = "id") UUID id, @RequestBody @Valid MedicoRequestDto medicoRequestDto) {
        EntityModel<MedicoResponseDto> medicoDtoEntity = EntityModel.of(medicoService.updateMedico(id, medicoRequestDto));
        medicoDtoEntity.add(linkTo(methodOn(MedicoController.class).getMedicoById(id)).withSelfRel());

        return ResponseEntity.status(HttpStatus.OK).body(medicoDtoEntity);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedico(@PathVariable(value = "id") UUID id) {
        medicoService.deleteMedico(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
