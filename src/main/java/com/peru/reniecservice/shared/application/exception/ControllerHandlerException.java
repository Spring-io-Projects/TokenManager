package com.peru.reniecservice.shared.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class ControllerHandlerException {
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessageException resourceNotFoundException(ResourceNotFoundException exception, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", exception.getMessage());
        return new ErrorMessageException(
                HttpStatus.NOT_FOUND.value(),
                request.getDescription(false),
                LocalDateTime.now(),
                errors
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessageException handleValidationExceptions(MethodArgumentNotValidException exception, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(
                error -> errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ErrorMessageException(
                HttpStatus.BAD_REQUEST.value(),
                request.getDescription(false),
                LocalDateTime.now(),
                errors
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessageException globalExceptionHandler(Exception exception, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", exception.getMessage());
        return new ErrorMessageException(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getDescription(false),
                LocalDateTime.now(),
                errors
        );
    }
}
