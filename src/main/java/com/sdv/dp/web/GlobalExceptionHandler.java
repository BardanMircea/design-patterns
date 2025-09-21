package com.sdv.dp.web;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,Object> body = new HashMap<>();
        body.put("error", "Validation failed");
        body.put("details", ex.getBindingResult().getFieldErrors().stream()
                .map(f -> Map.of("field", f.getField(), "message", f.getDefaultMessage())).toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String,String>> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(Map.of("error","Constraint violation", "details", ex.getMessage()));
    }

    @ExceptionHandler({ BadCredentialsException.class })
    public ResponseEntity<Map<String,String>> handleBadCreds(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Invalid credentials"));
    }

    @ExceptionHandler({ LockedException.class })
    public ResponseEntity<Map<String,String>> handleLocked(LockedException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String,String>> handleDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error","Forbidden"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> handleIllegal(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}

