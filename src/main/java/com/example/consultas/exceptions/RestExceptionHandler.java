package com.example.consultas.exceptions;

import jakarta.persistence.EntityExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {


    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<String> handleClienteNotFound(ClienteNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    @ExceptionHandler(MedicoNotFoundException.class)
    public ResponseEntity<String> handleMedicoNotFound(MedicoNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    @ExceptionHandler(ConsultaNotFoundException.class)
    public ResponseEntity<String> handleConsultaNotFound(ConsultaNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    @ExceptionHandler(MedicoEnderecoNotFoundException.class)
    public  ResponseEntity<String> handleMedicoEnderecoNotFound(MedicoEnderecoNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    @ExceptionHandler(ClienteEnderecoNotFoundException.class)
    public ResponseEntity<String> handleClienteEnderecoNotFound(ClienteEnderecoNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }


    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<String> handleEntityExists(EntityExistsException e){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach((fieldError) -> {errors.put(fieldError.getField(), fieldError.getDefaultMessage());});

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
