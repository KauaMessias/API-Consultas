package com.example.consultas.controllers;

import com.example.consultas.dtos.AuthenticationDto;
import com.example.consultas.dtos.LoginResponseDto;
import com.example.consultas.models.UsuarioModel;
import com.example.consultas.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> loginUsuario(@RequestBody @Valid AuthenticationDto authenticationDto) {
        UsernamePasswordAuthenticationToken usernamePassword = new UsernamePasswordAuthenticationToken(authenticationDto.email(), authenticationDto.senha());
        Authentication auth = authenticationManager.authenticate(usernamePassword);

        String token = tokenService.gerarToken((UsuarioModel) auth.getPrincipal());

        return ResponseEntity.ok().body(new LoginResponseDto(token));
    }

}
