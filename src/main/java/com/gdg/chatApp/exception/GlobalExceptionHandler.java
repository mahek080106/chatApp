package com.gdg.chatApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

// Turns exceptions into clean JSON error responses
@RestControllerAdvice
public class GlobalExceptionHandler {

    // @Valid failed -> 400 with a message per field
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("error", "Validation failed");
        body.put("fields", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // We throw ResponseStatusException in services (404, 409, 403 ...)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleStatus(
            ResponseStatusException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    // Broken / missing JSON body -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(
            HttpMessageNotReadableException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Request body is missing or not valid JSON");
        return ResponseEntity.badRequest().body(body);
    }
}
