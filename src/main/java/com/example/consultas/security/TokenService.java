package com.example.consultas.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.consultas.models.UsuarioModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    private static final long ACCESS_TOKEN_EXPIRATION = 10L;
    private static final long REFRESH_TOKEN_EXPIRATION = 10080L;

    public String gerarAccessToken(UsuarioModel usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("API-Consultas")
                    .withSubject(usuario.getEmail())
                    .withClaim("TYPE", "ACCESS")
                    .withExpiresAt(gerarExpiracao(ACCESS_TOKEN_EXPIRATION))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar Token.", exception);
        }
    }

    public String validarAccessToken(String token) {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("API-Consultas")
                    .withClaim("TYPE", "ACCESS")
                    .build()
                    .verify(token)
                    .getSubject();
    }

    public String validarRefreshToken(String token) {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("API-Consultas")
                    .withClaim("TYPE", "REFRESH")
                    .build()
                    .verify(token)
                    .getSubject();
    }

    public String gerarRefreshToken(UsuarioModel usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("API-Consultas")
                    .withSubject(usuario.getEmail())
                    .withClaim("TYPE", "REFRESH")
                    .withExpiresAt(gerarExpiracao(REFRESH_TOKEN_EXPIRATION))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar Token", exception);
        }
    }


    private Instant gerarExpiracao(long minutos) {
        return Instant.now().plusSeconds(minutos * 60);
    }
}
