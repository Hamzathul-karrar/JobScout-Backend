// Enhanced JobSearchController.java
package com.hamza.JobScout.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hamza.JobScout.dto.ApiResponse;
import com.hamza.JobScout.entity.JobResult;
import com.hamza.JobScout.entity.User;
import com.hamza.JobScout.exception.ApiKeyException;
import com.hamza.JobScout.exception.ApiLimitExceededException;
import com.hamza.JobScout.exception.SerpApiException;
import com.hamza.JobScout.service.JobSearchService;
import com.hamza.JobScout.service.UserService;

@RestController
@RequestMapping("/searchJob")
public class JobSearchController {
    
    private final JobSearchService jobSearchService;
    
    @Autowired
    private UserService userService;
    
    public JobSearchController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResult>>> searchJobsGoogle(
            @RequestParam String jobTitle,
            @RequestParam String location) {
        
        try {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            
            // Get user to extract userId
            User currentUser = userService.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));
            
            List<JobResult> results = jobSearchService.searchJobs(jobTitle, location, currentUser.getId());
            
            
            return ResponseEntity.ok(
                ApiResponse.success(results, "Jobs retrieved successfully", currentUser.getApiCallCount())
            );
            
        } catch (Exception e) {
            // This will be caught by exception handlers
            throw e;
        }
    }
    
    @ExceptionHandler(ApiKeyException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiKeyException(ApiKeyException e) {
        System.err.println("API Key Error: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(e.getUserMessage(), e.getErrorCode()));
    }
    
    @ExceptionHandler(ApiLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiLimitExceededException(ApiLimitExceededException e) {
        System.err.println("API Limit Exceeded: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ApiResponse.error(e.getUserMessage(), e.getErrorCode()));
    }
    
    @ExceptionHandler(SerpApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleSerpApiException(SerpApiException e) {
        System.err.println("SERP API Error: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getUserMessage(), e.getErrorCode()));
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        System.err.println("Runtime Error: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    "An unexpected error occurred. Please try again later.", 
                    "INTERNAL_ERROR"
                ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception e) {
        System.err.println("Generic Error: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                    "An unexpected error occurred. Please try again later.", 
                    "INTERNAL_ERROR"
                ));
    }
}
