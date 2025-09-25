// Enhanced SerpApiService.java
package com.hamza.JobScout.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.hamza.JobScout.exception.ApiKeyException;
import com.hamza.JobScout.exception.ApiLimitExceededException;
import com.hamza.JobScout.exception.SerpApiException;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SerpApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger logger = LoggerFactory.getLogger(SerpApiService.class);

    @Autowired
    private UserService userService;

    @Value("${serapi.max.page}")
    private int MAX_PAGES;

    public List<Map<String, Object>> searchJobs(String jobTitle, String location, String timeFilter, Long userId) {
        List<Map<String, Object>> allResponses = new ArrayList<>();
        return searchJobsWithPagination(jobTitle, location, timeFilter, 1, allResponses, userId);
    }

    @Transactional
    private List<Map<String, Object>> searchJobsWithPagination(String jobTitle, String location,
            String timeFilter, int currentPage, List<Map<String, Object>> allResponses, Long userId) {
        logger.info("Fetching SerpAPI page {} of max {}", currentPage, MAX_PAGES);
        
        if (currentPage > MAX_PAGES) {
            logger.info("Reached maximum page limit of {}", MAX_PAGES);
            return allResponses;
        }

        try {
            // Get API key from database for the current user
            String apiKey = userService.getApiKeyForUser(userId);
            
            // Validate API key exists
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new ApiKeyException(
                    "User API key is missing",
                    "Please configure your SERP API key in your profile settings"
                );
            }

            String url = buildSearchUrl(jobTitle, location, timeFilter, currentPage, apiKey);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            // Check for SERP API specific errors first
            validateSerpApiResponse(response);
            
            // If no SERP API error, increment the API call counter
            // This counts successful calls and calls with DMCA messages but no actual SERP API errors
            userService.incrementApiCallCount(userId);
            logger.info("API call count incremented for user ID: {}", userId);

            if (response != null) {
                allResponses.add(response);
                
                // Check for DMCA message indicating filtered/omitted results
                if (hasDmcaMessage(response)) {
                    logger.info("Google has omitted similar results. Stopping pagination.");
                    return allResponses;
                }

                // Continue pagination if next page exists
                if (hasNextPage(response) && currentPage < MAX_PAGES) {
                    return searchJobsWithPagination(jobTitle, location, timeFilter,
                            currentPage + 1, allResponses, userId);
                }
            }
            
            return allResponses;
            
        } catch (HttpClientErrorException e) {
            handleHttpClientError(e, currentPage);
            return allResponses;
        } catch (HttpServerErrorException e) {
            logger.error("SERP API server error on page {}: {}", currentPage, e.getMessage());
            throw new SerpApiException(
                "SERP API server error: " + e.getMessage(),
                "SERVER_ERROR",
                "The search service is temporarily unavailable. Please try again later."
            );
        } catch (RestClientException e) {
            logger.error("Error calling SerpAPI on page {}: {}", currentPage, e.getMessage());
            throw new SerpApiException(
                "Network error: " + e.getMessage(),
                "NETWORK_ERROR",
                "Unable to connect to search service. Please check your internet connection and try again."
            );
        } catch (SerpApiException e) {
            // Re-throw custom exceptions (these are SERP API errors, don't count)
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error on page {}: {}", currentPage, e.getMessage());
            throw new SerpApiException(
                "Unexpected error: " + e.getMessage(),
                "UNEXPECTED_ERROR",
                "An unexpected error occurred while searching for jobs. Please try again."
            );
        }
    }

    private void validateSerpApiResponse(Map<String, Object> response) {
        if (response == null) {
            throw new SerpApiException(
                "Empty response from SERP API",
                "API_ERROR",
                "Search service returned empty response. Please try again."
            );
        }

        // Check for error in response
        Object error = response.get("error");
        if (error != null) {
            String errorMessage = error.toString();
            logger.error("SERP API Error: {}", errorMessage);
            
            // Handle specific SERP API errors
            if (errorMessage.toLowerCase().contains("invalid api key") ||
                errorMessage.toLowerCase().contains("api key") ||
                errorMessage.toLowerCase().contains("authentication")) {
                throw new ApiKeyException(
                    "Invalid SERP API key: " + errorMessage,
                    "Your SERP API key is invalid or expired."
                );
            } else if (errorMessage.toLowerCase().contains("limit") ||
                       errorMessage.toLowerCase().contains("quota") ||
                       errorMessage.toLowerCase().contains("credit")) {
                throw new ApiLimitExceededException(
                    "SERP API limit exceeded: " + errorMessage,
                    "You have reached your SERP API usage limit. Please check your SERP API account or upgrade your plan."
                );
            } else {
                throw new SerpApiException(
                    "SERP API error: " + errorMessage,
                    "API_ERROR",
                    "Search service error: " + errorMessage
                );
            }
        }

        // Check for specific SERP API status indicators
        Object searchMetadata = response.get("search_metadata");
        if (searchMetadata instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) searchMetadata;
            Object status = metadata.get("status");
            
            if ("Error".equals(status)) {
                Object errorMsg = metadata.get("error_message");
                String errorMessage = errorMsg != null ? errorMsg.toString() : "Unknown API error";
                
                if (errorMessage.toLowerCase().contains("api key")) {
                    throw new ApiKeyException(
                        "SERP API key error: " + errorMessage,
                        "Your SERP API key is invalid. Please check and update your API key."
                    );
                } else {
                    throw new SerpApiException(
                        "SERP API status error: " + errorMessage,
                        "API_ERROR",
                        "Search failed: " + errorMessage
                    );
                }
            }
        }
    }

    private void handleHttpClientError(HttpClientErrorException e, int currentPage) {
        int statusCode = e.getStatusCode().value();
        String responseBody = e.getResponseBodyAsString();
        logger.error("HTTP {} error on page {}: {}", statusCode, currentPage, responseBody);
        
        switch (statusCode) {
            case 401:
                throw new ApiKeyException(
                    "Unauthorized SERP API request: " + responseBody,
                    "Your SERP API key is invalid or expired."
                );
            case 403:
                throw new ApiLimitExceededException(
                    "SERP API access forbidden: " + responseBody,
                    "Access to SERP API is forbidden. You may have exceeded your usage limit or your account may be suspended."
                );
            case 429:
                throw new ApiLimitExceededException(
                    "SERP API rate limit exceeded: " + responseBody,
                    "You have made too many requests. Please wait a moment and try again."
                );
            default:
                throw new SerpApiException(
                    "SERP API HTTP error " + statusCode + ": " + responseBody,
                    "HTTP_ERROR",
                    "Search service returned error " + statusCode + ". Please try again."
                );
        }
    }

    private String buildSearchUrl(String jobTitle, String location, String timeFilter, int page, String apiKey) {
        String query = jobTitle + " " + location + " site:*/careers " +
                "-site:naukri.com -site:indeed.com -site:monsterindia.com -site:shine.com " +
                "-site:timesjobs.com -site:glassdoor.co.in -site:linkedin.com -site:freshersworld.com " +
                "-site:hirist.com -site:foundit.in -site:simplyhired.co.in -site:internshala.com " +
                "-site:elitmus.com -site:placementindia.com -site:apna.co -site:way2fresher.com " +
                "-site:jumpwhere.com -site:in.prosple.com";

        String baseUrl = "https://serpapi.com/search.json?engine=google" +
                "&q=" + query +
                "&tbs=" + timeFilter +
                "&api_key=" + apiKey;

        if (page > 1) {
            int startIndex = (page - 1) * 10;
            baseUrl += "&start=" + startIndex;
        }

        return baseUrl;
    }

    private boolean hasNextPage(Map<String, Object> response) {
        if (response == null) return false;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> serpApiPagination = (Map<String, Object>) response.get("serpapi_pagination");
        return serpApiPagination != null && serpApiPagination.containsKey("next");
    }

    private boolean hasDmcaMessage(Map<String, Object> response) {
        if (response == null) return false;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> dmcaMessages = (Map<String, Object>) response.get("dmca_messages");
        if (dmcaMessages == null) return false;
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) dmcaMessages.get("messages");
        if (messages == null || messages.isEmpty()) return false;
        
        for (Map<String, Object> message : messages) {
            Object content = message.get("content");
            if (content != null) {
                String contentStr = content.toString().toLowerCase();
                if (contentStr.contains("omitted some entries") || contentStr.contains("very similar to")) {
                    return true;
                }
            }
        }
        return false;
    }

    // New method to get current API call count for a user
    public Integer getUserApiCallCount(Long userId) {
        return userService.getCurrentApiCallCount(userId);
    }
}
