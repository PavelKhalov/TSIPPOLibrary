package ru.khalov.tsippolibrary.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.ErrorResponseException;
import ru.khalov.tsippolibrary.util.expeption.UsernameTakenException;

import java.time.Instant;
import java.util.Map;

/**
 * Единый формат ошибок — ТЗ 3.2.4: код, сообщение, время.
 * Также пробрасываем ProblemDetail для кастомных исключений.
 */
@RestControllerAdvice
public class GlobarExceptionHandler {

    // 409 — уже существует (регистрация)
    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ProblemDetail> usernameTaken(UsernameTakenException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getBody());
    }

    // Проброс всех ErrorResponseException (наши кастомные: 404, 409 и т.д.)
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ProblemDetail> errorResponse(ErrorResponseException e) {
        ProblemDetail body = e.getBody();
        return ResponseEntity.status(body.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Validation failed",
                "messages", errors,
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> constraint(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Validation failed",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> badCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", "Invalid email or password",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> disabled(DisabledException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", "Account is disabled",
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> accessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "status", 403,
                "error", "Forbidden",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> illegalArg(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> illegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", 409,
                "error", "Conflict",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }
}
