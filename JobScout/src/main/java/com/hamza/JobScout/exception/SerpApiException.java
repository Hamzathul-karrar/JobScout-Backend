package com.hamza.JobScout.exception;

public class SerpApiException extends RuntimeException {
    private String errorCode;
    private String userMessage;
    
    public SerpApiException(String message, String errorCode, String userMessage) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = userMessage;
    }
    
    public String getErrorCode() { return errorCode; }
    public String getUserMessage() { return userMessage; }
}