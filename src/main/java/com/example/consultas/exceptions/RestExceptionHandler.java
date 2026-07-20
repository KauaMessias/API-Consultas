    package com.example.consultas.exceptions;

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

        @ExceptionHandler({RefreshTokenNotFoundException.class, HorarioNotFoundException.class, ClienteNotFoundException.class, TokenValidacaoNotFoundException.class, MedicoNotFoundException.class, ConsultaNotFoundException.class, EnderecoNotFoundException.class, UsuarioNotFoundException.class})
        public ResponseEntity<ErrorDto> handleNotFoundExceptions(RuntimeException e, HttpServletRequest request) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorDto(LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(), e.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler({UsuarioInativoException.class, TokenValidacaoExpiradoException.class, ConflitoConsultaException.class})
        public ResponseEntity<ErrorDto> handleBadRequestExceptions(RuntimeException e, HttpServletRequest request) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDto(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), e.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler({UsuarioValidacaoException.class, DataIntegrityViolationException.class, EntityExistsException.class, SenhaIncorretaException.class})
        public ResponseEntity<ErrorDto> handleConflictExceptions(RuntimeException e, HttpServletRequest request) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorDto(LocalDateTime.now(), HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.name(), e.getMessage(), request.getRequestURI()));
        }

        @ExceptionHandler(RefreshTokenRevokedException.class)
        public ResponseEntity<ErrorDto> handleRefreshTokenRevoked(RefreshTokenRevokedException e, HttpServletRequest request) {
            log.warn(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorDto(LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.name(), e.getMessage(), request.getRequestURI()));
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
