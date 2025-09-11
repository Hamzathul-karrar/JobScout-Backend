package com.hamza.JobScout.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import java.util.*;

@Service
public class SerpApiService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${serpapi.api.key}")
    private String API_KEY;
    
    private final int MAX_PAGES = 1;
    
    public List<Map<String, Object>> searchJobs(String jobTitle, String location, String timeFilter) {
        List<Map<String, Object>> allResponses = new ArrayList<>();
        return searchJobsWithPagination(jobTitle, location, timeFilter, 1, allResponses);
    }
    
    private List<Map<String, Object>> searchJobsWithPagination(String jobTitle, String location, 
            String timeFilter, int currentPage, List<Map<String, Object>> allResponses) {
        
        System.out.println("Fetching SerpAPI page " + currentPage + " of max " + MAX_PAGES);
        
        if (currentPage > MAX_PAGES) {
            System.out.println("Reached maximum page limit of " + MAX_PAGES);
            return allResponses;
        }
        
        try {
            String url = buildSearchUrl(jobTitle, location, timeFilter, currentPage);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                allResponses.add(response);
                
                // Check for DMCA message indicating filtered/omitted results
                if (hasDmcaMessage(response)) {
                    System.out.println("Google has omitted similar results. Stopping pagination.");
                    return allResponses;
                }
                
                // Continue pagination if next page exists
                if (hasNextPage(response) && currentPage < MAX_PAGES) {
                    return searchJobsWithPagination(jobTitle, location, timeFilter, 
                        currentPage + 1, allResponses);
                }
            }
            
            return allResponses;
            
        } catch (RestClientException e) {
            System.err.println("Error calling SerpAPI on page " + currentPage + ": " + e.getMessage());
            return allResponses;
        } catch (Exception e) {
            System.err.println("Unexpected error on page " + currentPage + ": " + e.getMessage());
            return allResponses;
        }
    }
    
    private String buildSearchUrl(String jobTitle, String location, String timeFilter, int page) {
        String query = jobTitle + " " + location + "\" site:*/careers "
                + "-site:naukri.com -site:indeed.com -site:monsterindia.com -site:shine.com "
                + "-site:timesjobs.com -site:glassdoor.co.in -site:linkedin.com -site:freshersworld.com "
                + "-site:hirist.com -site:foundit.in -site:simplyhired.co.in -site:internshala.com "
                + "-site:elitmus.com -site:placementindia.com -site:apna.co -site:way2fresher.com "
                + "-site:jumpwhere.com -site:in.prosple.com";
        
        String baseUrl = "https://serpapi.com/search.json?engine=google" 
                + "&q=" + query 
                + "&tbs=" + timeFilter 
                + "&api_key=" + API_KEY;
        
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
}
