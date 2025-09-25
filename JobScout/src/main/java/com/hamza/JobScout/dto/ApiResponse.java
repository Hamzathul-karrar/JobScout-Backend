package com.hamza.JobScout.dto;

public class ApiResponse<T> {
    
    private String message;
    private boolean success;
    private String errorCode;
    private T data;
    
    // NEW: Optional field for API call count - backward compatible
    private Integer apiCallCount;
    
    // Existing constructors remain unchanged for backward compatibility
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
    
    // NEW: Constructor with API call count
    public ApiResponse(T data, String message, boolean success, Integer apiCallCount) {
        this.data = data;
        this.message = message;
        this.success = success;
        this.apiCallCount = apiCallCount;
    }
    
    // Static factory methods for common responses (existing remain unchanged)
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(data, message, true);
    }
    
    // NEW: Success method with API call count
    public static <T> ApiResponse<T> success(T data, String message, Integer apiCallCount) {
        return new ApiResponse<>(data, message, true, apiCallCount);
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(message, false, errorCode);
    }
    
    // Existing getters and setters remain unchanged
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    // NEW: Getter and setter for API call count
    public Integer getApiCallCount() { return apiCallCount; }
    public void setApiCallCount(Integer apiCallCount) { this.apiCallCount = apiCallCount; }
}
