package com.example.consultas.exceptions;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {


    @ExceptionHandler(exception = {ClienteNotFoundException.class, MedicoNotFoundException.class, ConsultaNotFoundException.class, EnderecoNotFoundException.class, UsuarioNotFoundException.class})
    public ResponseEntity<ErrorDto> handleNotFoundException(RuntimeException e, HttpServletRequest request) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(UsuarioInativoException.class)
    public ResponseEntity<ErrorDto> handleUsuarioInativoException(UsuarioInativoException e, HttpServletRequest request) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(ConflitoConsultaException.class)
    public ResponseEntity<ErrorDto> handleConflitoConsulta(ConflitoConsultaException e, HttpServletRequest request) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.name(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.name(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ErrorDto> handleRefreshTokenRevoked(RefreshTokenRevokedException e, HttpServletRequest request) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDto(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorDto> handleEntityExists(EntityExistsException e, HttpServletRequest request) {
        log.warn(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.name(), e.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach((fieldError) -> {
                    errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
