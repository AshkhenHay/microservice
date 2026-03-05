package com.epam.songservice.exception;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ErrorDto {
    private String errorMessage;
    private Map<String, String> details;
    private String errorCode;

    public ErrorDto(String errorMessage, String errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public ErrorDto(String errorMessage, Map<String, String> details, String errorCode) {
        this.errorMessage = errorMessage;
        this.details = details;
        this.errorCode = errorCode;
    }
}

