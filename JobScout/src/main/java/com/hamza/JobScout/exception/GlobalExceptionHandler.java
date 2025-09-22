// GlobalExceptionHandler.java
package com.hamza.JobScout.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.hamza.JobScout.dto.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ApiKeyException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiKeyException(
            ApiKeyException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getUserMessage(), e.getErrorCode()));
    }
    
    @ExceptionHandler(ApiLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiLimitExceededException(
            ApiLimitExceededException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(e.getUserMessage(), e.getErrorCode()));
    }
    
    @ExceptionHandler(SerpApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleSerpApiException(
            SerpApiException e, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getUserMessage(), e.getErrorCode()));
    }
}
