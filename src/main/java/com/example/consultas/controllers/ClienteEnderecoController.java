package com.example.consultas.controllers;

import com.example.consultas.dtos.ClienteEnderecoDto;
import com.example.consultas.services.ClienteEnderecoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cliente/endereco")
public class ClienteEnderecoController {

    private final ClienteEnderecoService clienteEnderecoService;

    public ClienteEnderecoController(ClienteEnderecoService clienteEnderecoService) {
        this.clienteEnderecoService = clienteEnderecoService;
    }


    @PostMapping
    public ResponseEntity<ClienteEnderecoDto> addClienteEndereco(@RequestBody ClienteEnderecoDto clienteEnderecoDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteEnderecoService.addClienteEndereco(clienteEnderecoDto));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClienteEnderecoDto> getClienteEnderecoById(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(clienteEnderecoService.getClienteEnderecoById(id));
    }


    @GetMapping("/cliente/{clienteid}")
    public ResponseEntity<List<ClienteEnderecoDto>> getClienteEnderecos(@PathVariable(value = "clienteid") UUID clienteid) {
        return ResponseEntity.status(HttpStatus.OK).body(clienteEnderecoService.getClienteEnderecoByClienteID(clienteid));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ClienteEnderecoDto> updateClienteEndereco(@PathVariable(value = "id") UUID id, @RequestBody ClienteEnderecoDto clienteEnderecoDto) {
        return ResponseEntity.status(HttpStatus.OK).body(clienteEnderecoService.updateClienteEndereco(id, clienteEnderecoDto));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClienteEndereco(@PathVariable(value = "id") UUID id) {
        clienteEnderecoService.deleteClienteEndereco(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}