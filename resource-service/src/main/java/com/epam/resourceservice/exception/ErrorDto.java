package com.epam.resourceservice.exception;

import lombok.Data;

import java.util.Map;

@Data
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
