package com.hamza.JobScout.exception;

public class ApiLimitExceededException extends SerpApiException {
    public ApiLimitExceededException(String message, String userMessage) {
        super(message, "API_LIMIT_EXCEEDED", userMessage);
    }
}