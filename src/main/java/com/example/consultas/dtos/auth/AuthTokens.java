package com.example.consultas.dtos.auth;

public record AuthTokens(String accessToken, String refreshToken, String authorities) {
}
