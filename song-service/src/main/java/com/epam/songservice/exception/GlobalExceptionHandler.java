package com.epam.songservice.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> notFound(NotFoundException ex) {
        return ResponseEntity.status(404)
                .body(new ErrorDto(ex.getMessage(), "404"));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDto> badReq(ValidationException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorDto(ex.getMessage(), "400"));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorDto> conflict(ConflictException ex) {
        return ResponseEntity.status(409)
                .body(new ErrorDto(ex.getMessage(), "409"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> serverErr(Exception ex) {
        return ResponseEntity.status(500)
                .body(new ErrorDto("An error occurred on the server", "500"));
    }
}