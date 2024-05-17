package com.simplyfelipe.microid.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public ServiceException(HttpStatus status, String message) {
        super(message);
        this.message = message;
        this.status = status;
    }
}
