package com.example.consultas.controllers;
import com.example.consultas.dtos.auth.AuthTokens;
import com.example.consultas.dtos.auth.AuthenticationDto;
import com.example.consultas.dtos.auth.AuthResponseDto;
import com.example.consultas.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private static final long REFRESH_TOKEN_EXPIRATION = 7L * 24 * 60 * 60;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> loginUsuario(@RequestBody @Valid AuthenticationDto authDto) {

        AuthTokens authTokens = authService.login(authDto);

        ResponseCookie cookie = gerarCookie(authTokens.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponseDto(authTokens.accessToken(), authTokens.authorities()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@CookieValue("refreshToken") String refreshToken) {
        AuthTokens authTokens = authService.renovarToken(refreshToken);
        ResponseCookie cookie = gerarCookie(authTokens.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponseDto(authTokens.accessToken(), authTokens.authorities()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("refreshToken") String refreshToken) {
        authService.revogarToken(refreshToken);
        ResponseCookie cookie = limparRefreshCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(null);
    }


    private ResponseCookie gerarCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(REFRESH_TOKEN_EXPIRATION)
                .sameSite("None")
                .build();
    }

    private ResponseCookie limparRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
    }
}
