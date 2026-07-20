    package com.example.consultas.exceptions;

    import org.springframework.validation.FieldError;

    import java.time.LocalDateTime;


    public record ErrorDto(LocalDateTime timestamp, int status, String error, String message, String path) {
    }
