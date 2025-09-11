package com.hamza.JobScout.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "SerpApi key is required")
    private String serpapiKey;

    // Constructors, getters, and setters...
    public RegisterRequest() {}

    public RegisterRequest(String fullName, String email, String password, String serpapiKey) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.serpapiKey = serpapiKey;
    }

    // Getters and setters...
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getSerpapiKey() { return serpapiKey; }
    public void setSerpapiKey(String serpapiKey) { this.serpapiKey = serpapiKey; }
}
