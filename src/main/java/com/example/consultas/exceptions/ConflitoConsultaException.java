package com.example.consultas.exceptions;

public class ConflitoConsultaException extends RuntimeException {
    public ConflitoConsultaException(String message) {
        super(message);
    }
    public ConflitoConsultaException() {
        super("Conflito no horário da consulta.");
    }
}
