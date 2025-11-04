package com.example.consultas.controllers;

import com.example.consultas.dtos.ConsultaDto;
import com.example.consultas.services.ConsultaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService consultaService;

    public ConsultaController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }


    @PostMapping
    public ResponseEntity<ConsultaDto> addConsulta(@RequestBody ConsultaDto consultaDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.addConsulta(consultaDto));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ConsultaDto> getConsulta(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaById(id));
    }


    @GetMapping("/medico/{medicoid}")
    public ResponseEntity<List<ConsultaDto>> getConsultaByMedicoId(@PathVariable(value = "medicoid") UUID medicoid) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaByMedicoId(medicoid));
    }


    @GetMapping("/cliente/{clienteid}")
    public ResponseEntity<List<ConsultaDto>> getConsultaByClienteId(@PathVariable(value = "clienteid") UUID clienteid) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.getConsultaByClienteId(clienteid));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ConsultaDto> updateConsulta(@PathVariable UUID id, @RequestBody ConsultaDto consultaDto) {
        return ResponseEntity.status(HttpStatus.OK).body(consultaService.updateConsulta(id, consultaDto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsulta(@PathVariable UUID id) {
        consultaService.deleteConsulta(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
