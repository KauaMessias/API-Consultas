package com.example.consultas.controllers;

import com.example.consultas.dtos.ConsultaDto;
import com.example.consultas.services.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }


    @PostMapping
    public ResponseEntity<ConsultaDto> addConsulta(@RequestBody @Valid ConsultaDto consultaDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.addConsulta(consultaDto));
    }


    @GetMapping("/{id}")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<ConsultaDto> getConsulta(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaById(id));
    }


    @GetMapping("/medico/{medico_id}")
    @PreAuthorize("@authz.autorizado(#medico_id, authentication)")
    public ResponseEntity<List<ConsultaDto>> getConsultaByMedicoId(@PathVariable(value = "medico_id") UUID medico_id) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaByMedicoId(medico_id));
    }


    @GetMapping("/cliente/{cliente_id}")
    @PreAuthorize("@authz.autorizado(#cliente_id, authentication)")
    public ResponseEntity<List<ConsultaDto>> getConsultaByClienteId(@PathVariable(value = "cliente_id") UUID cliente_id) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaByClienteId(cliente_id));
    }


    @PutMapping("/{id}")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<ConsultaDto> updateConsulta(@PathVariable(value = "id") UUID id, @RequestBody @Valid ConsultaDto consultaDto) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.updateConsulta(id, consultaDto));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("@authz.acessoConsulta(#id, authentication)")
    public ResponseEntity<Void> deleteConsulta(@PathVariable UUID id) {
        consultaService.deleteConsulta(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
