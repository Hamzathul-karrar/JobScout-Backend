package com.hamza.JobScout.exception;

public class ApiKeyException extends SerpApiException {
    public ApiKeyException(String message, String userMessage) {
        super(message, "API_KEY_ERROR", userMessage);
    }
}