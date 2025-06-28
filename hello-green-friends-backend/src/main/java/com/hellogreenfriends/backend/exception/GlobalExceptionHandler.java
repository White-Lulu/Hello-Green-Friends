package com.hellogreenfriends.backend.exception;

import com.hellogreenfriends.backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        return new ResponseEntity<>(ApiResponse.failure(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ApiResponse.failure(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse> handleSecurityException(SecurityException ex) {
        return new ResponseEntity<>(ApiResponse.failure(ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGlobalException(Exception ex) {
        ex.printStackTrace();
        return new ResponseEntity<>(ApiResponse.failure("An unexpected internal server error occurred."), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}