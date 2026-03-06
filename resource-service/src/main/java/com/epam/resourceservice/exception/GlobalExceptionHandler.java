package com.epam.resourceservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ServerErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> notFound(NotFoundException ex) {
        log.debug("NotFound: {}", ex.getMessage());
        return ResponseEntity.status(404)
                .body(new ErrorDto(ex.getMessage(), "404"));
    }

    @ExceptionHandler(ChangeSetPersister.NotFoundException.class)
    public ResponseEntity<ErrorDto> changeSetNotFound(ChangeSetPersister.NotFoundException ex) {
        log.debug("NotFound: {}", ex.getMessage());
        return ResponseEntity.status(404)
                .body(new ErrorDto(ex.getMessage(), "404"));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDto> handleValidationException(ValidationException ex) {
        log.debug("ValidationException: {}", ex.getMessage());
        Map<String, String> errors = ex.getErrors();
        if (errors != null && !errors.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ErrorDto(ex.getMessage(), errors, "400"));
        }
        return ResponseEntity.badRequest()
                .body(new ErrorDto(ex.getMessage(), "400"));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorDto> handleBindingErrors(Exception ex) {
        log.debug("Binding/validation error: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        if (ex instanceof MethodArgumentNotValidException manv) {
            for (FieldError fe : manv.getBindingResult().getFieldErrors()) {
                details.put(fe.getField(), fe.getDefaultMessage());
            }
        } else if (ex instanceof BindException be) {
            for (FieldError fe : be.getBindingResult().getFieldErrors()) {
                details.put(fe.getField(), fe.getDefaultMessage());
            }
        }
        return ResponseEntity.badRequest()
                .body(new ErrorDto("Validation error", details, "400"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDto> handleConstraintViolation(ConstraintViolationException ex) {
        log.debug("Constraint violations: {}", ex.getMessage());
        Map<String, String> details = new HashMap<>();
        for (ConstraintViolation<?> cv : ex.getConstraintViolations()) {
            String path = cv.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            details.put(field, cv.getMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErrorDto("Validation error", details, "400"));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.debug("Type mismatch for parameter {}: {}", ex.getName(), ex.getMessage());
        String value = ex.getValue() != null ? ex.getValue().toString() : "unknown";
        String msg = String.format("Invalid value '%s' for ID. Must be a positive integer", value);
        return ResponseEntity.badRequest()
                .body(new ErrorDto(msg, "400"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleUnreadable(HttpMessageNotReadableException ex) {
        log.debug("Unreadable message: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorDto("Malformed request body", "400"));
    }

    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorDto> handleMediaTypeNotSupported(org.springframework.web.HttpMediaTypeNotSupportedException ex) {
        log.debug("Unsupported media type: {}", ex.getMessage());
        String msg = "Invalid file format: " + ex.getContentType() + ". Only MP3 files are allowed";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(msg, "400"));
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<ErrorDto> serverErr(ServerErrorException ex) {
        log.error("Internal server error", ex);
        return ResponseEntity.status(500)
                .body(new ErrorDto("An error occurred on the server", "500"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(500)
                .body(new ErrorDto("An error occurred on the server", "500"));
    }
}

