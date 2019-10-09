package de.magicline.racoon.api.dto;

import org.springframework.web.server.ResponseStatusException;

public class ErrorResponse {

    private final int status;
    private final String message;

    public ErrorResponse(ResponseStatusException e) {
        this(e.getStatus().value(), e.getMessage());
    }

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
