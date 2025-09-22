package com.hamza.JobScout.dto;

public class ApiResponse<T> {
    private String message;
    private boolean success;
    private String errorCode;
    private T data;
    
    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
    
    public ApiResponse(String message, boolean success, String errorCode) {
        this.message = message;
        this.success = success;
        this.errorCode = errorCode;
    }
    
    public ApiResponse(T data, String message, boolean success) {
        this.data = data;
        this.message = message;
        this.success = success;
    }
    
    // Static factory methods for common responses
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, true);
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(message, false, errorCode);
    }
    
    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
