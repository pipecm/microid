package com.simplyfelipe.microid.exception;

import com.simplyfelipe.microid.response.ServiceResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class ServiceExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ServiceException.class)
    protected ResponseEntity<Object> handleServiceException(ServiceException exception, WebRequest request) {
        return handleExceptionInternal(
                exception,
                ServiceResponse.builder()
                        .code(exception.getStatus().value())
                        .status(exception.getStatus().name())
                        .message(exception.getMessage())
                        .build(),
                new HttpHeaders(),
                exception.getStatus(),
                request
        );
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    protected ResponseEntity<Object> handleUsernameNotFoundException(UsernameNotFoundException exception, WebRequest request) {
        return handleExceptionInternal(
                exception,
                ServiceResponse.builder()
                        .code(NOT_FOUND.value())
                        .status(NOT_FOUND.name())
                        .message(exception.getMessage())
                        .build(),
                new HttpHeaders(),
                NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(value = Exception.class)
    protected ResponseEntity<Object> handleOtherException(Exception exception, WebRequest request) {
        return handleExceptionInternal(
                exception,
                ServiceResponse.builder()
                        .code(INTERNAL_SERVER_ERROR.value())
                        .status(INTERNAL_SERVER_ERROR.name())
                        .message(exception.getMessage())
                        .build(),
                new HttpHeaders(),
                INTERNAL_SERVER_ERROR,
                request
        );
    }
}
