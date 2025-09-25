package com.hamza.JobScout.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class UserValidator {
	
    private static final Logger logger = LoggerFactory.getLogger(UserValidator.class);


	/**
	 * Validate and sanitize email
	 */
	public String validateAndSanitizeEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email is required");
		}
		String sanitized = email.toLowerCase().trim();
		if (!isValidEmail(sanitized)) {
			throw new IllegalArgumentException("Please provide a valid email address");
		}
		return sanitized;
	}

	/**
	 * Validate and sanitize full name
	 */
	public String validateAndSanitizeFullName(String fullName) {
		if (fullName == null || fullName.trim().isEmpty()) {
			throw new IllegalArgumentException("Full name is required");
		}
		String sanitized = fullName.trim();
		if (sanitized.length() < 2 || sanitized.length() > 100) {
			throw new IllegalArgumentException("Full name must be between 2 and 100 characters");
		}
		// Remove extra whitespaces between words
		sanitized = sanitized.replaceAll("\\s+", " ");
		return sanitized;
	}

	/**
	 * Validate password
	 */
	public String validatePassword(String password) {
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("Password is required");
		}
		if (!isValidPassword(password)) {
			throw new IllegalArgumentException(
					"Password must be at least 6 characters and contain letters and numbers");
		}
		return password;
	}

	/**
	 * Validate email format
	 */
	private boolean isValidEmail(String email) {
		if (email == null || email.isEmpty()) {
			return false;
		}

		// Basic email validation
		return email.contains("@") && email.contains(".") && email.length() > 5 && email.length() < 255
				&& !email.startsWith("@") && !email.endsWith("@") && !email.startsWith(".") && !email.endsWith(".")
				&& email.indexOf("@") != email.lastIndexOf("@") == false && // Only one @
				email.indexOf("@") < email.lastIndexOf("."); // @ comes before last .
	}

	/**
	 * Validate password strength
	 */
	private boolean isValidPassword(String password) {
		if (password == null || password.length() < 6) {
			return false;
		}

		boolean hasLetter = password.chars().anyMatch(Character::isLetter);
		boolean hasDigit = password.chars().anyMatch(Character::isDigit);

		return hasLetter && hasDigit;
	}

	/**
	 * sanitize SerpApi key
	 */
	public String validateAndSanitizeSerpapiKey(String serpapiKey) {
		if (serpapiKey == null || serpapiKey.trim().isEmpty()) {
			throw new IllegalArgumentException("SerpApi key is required");
		}
		String sanitized = serpapiKey.trim();

		// Basic validation for SerpApi key format (they are typically alphanumeric)
		if (sanitized.length() < 10 || sanitized.length() > 100) {
			throw new IllegalArgumentException("SerpApi key appears to be invalid");
		}

		boolean isValid = validateSerpApiKey(sanitized);
		if (!isValid) {
			throw new IllegalArgumentException("SerpApi key is not valid or has expired");
		}

		return sanitized;
	}

	/**
	 * Validate SerpApi key
	 */
	public boolean validateSerpApiKey(String apiKey) {
		String url = "https://serpapi.com/search.json?engine=google&q=serpapi&api_key=" + apiKey;
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper objectMapper = new ObjectMapper();

		try {
			String response = restTemplate.getForObject(url, String.class);
			JsonNode jsonNode = objectMapper.readTree(response);

			// Check for error field indicating invalid key or other issues
			if (jsonNode.has("error")) {
                logger.warn("SerpApi error: {}", jsonNode.get("error").asText());
				return false;
			}

			// Check for presence of organic_results indicating a valid response
			if (jsonNode.has("organic_results") && jsonNode.get("organic_results").isArray()) {
				return true;
			}

			// If no organic_results and no error field, treat as invalid or unexpected
			return false;

		} catch (HttpClientErrorException e) {
			// Handle HTTP errors like 401 Unauthorized
            logger.warn("HTTP error status: {}", e.getStatusCode());
			return false;
		} catch (ResourceAccessException e) {
			// Handle connection errors
            logger.warn("Resource access error: {}", e.getMessage());
			return false;
		} catch (Exception e) {
			// Handle other exceptions like JSON parsing errors
            logger.error("Exception during validation: {}", e.getMessage());
			return false;
		}
	}

	/**
	 * Validate login email (simpler validation for login)
	 */
	public String validateLoginEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			throw new IllegalArgumentException("Email is required");
		}
		return email.toLowerCase().trim();
	}

	/**
	 * Validate login password (basic validation for login)
	 */
	public String validateLoginPassword(String password) {
		if (password == null || password.isEmpty()) {
			throw new IllegalArgumentException("Password is required");
		}
		return password;
	}

}
