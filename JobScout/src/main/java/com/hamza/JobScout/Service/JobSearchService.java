package com.hamza.JobScout.Service; 
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.hamza.JobScout.Model.JobResult;

@Service
public class JobSearchService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String API_KEY = "969abe7e68ffeae2da405317ed076bbbd9379e0662e2e9eab01478795826f1e6";
    private final int MAX_PAGES = 20; // Limit to 10 pages
    
    public List<JobResult> searchJobsGoogle(String jobTitle, String location) {
        return searchJobsWithPagination(jobTitle, location, 1, new ArrayList<>());
    }
    
    private List<JobResult> searchJobsWithPagination(String jobTitle, String location, int currentPage, List<JobResult> allResults) {
        System.out.println("Searching page " + currentPage + " of max " + MAX_PAGES);
        
        if (currentPage > MAX_PAGES) {
            System.out.println("Reached maximum page limit of " + MAX_PAGES);
            return allResults;
        }
        
        try {
            String url = buildSearchUrl(jobTitle, location, currentPage);
//            System.out.println("URL generated: " + url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
//            System.out.println("API Response for page " + currentPage + ": " + response);
            
            // Process current page results
            List<JobResult> currentPageResults = processSearchResults(response, jobTitle, location);
            allResults.addAll(currentPageResults);
            
            System.out.println("Found " + currentPageResults.size() + " results on page " + currentPage);
            System.out.println("Total results so far: " + allResults.size());
            
            // Check if there's a next page and we haven't reached the limit
            if (hasNextPage(response) && currentPage <= MAX_PAGES) {
                String nextPageUrl = getNextPageUrl(response);
                if (nextPageUrl != null) {
                    // Recursive call for next page
                    return searchJobsWithPagination(jobTitle, location, currentPage + 1, allResults);
                }
            }
            
            System.out.println("Pagination completed. Total results: " + allResults.size());
            return allResults;
            
        } catch (RestClientException e) {
            System.err.println("Error calling SerpAPI on page " + currentPage + ": " + e.getMessage());
            return allResults; // Return what we have so far
        } catch (Exception e) {
            System.err.println("Unexpected error on page " + currentPage + ": " + e.getMessage());
            return allResults; // Return what we have so far
        }
    }
    
    private String buildSearchUrl(String jobTitle, String location, int page) {
        String query = jobTitle + " " + location + "\" site:*/careers " +
                "-site:naukri.com -site:indeed.com -site:monsterindia.com -site:shine.com " +
                "-site:timesjobs.com -site:glassdoor.co.in -site:linkedin.com -site:freshersworld.com " +
                "-site:hirist.com -site:foundit.in -site:simplyhired.co.in -site:internshala.com " +
                "-site:elitmus.com -site:placementindia.com -site:apna.co -site:way2fresher.com " +
                "-site:jumpwhere.com -site:in.prosple.com";
        
        String baseUrl = "https://serpapi.com/search.json?engine=google" +
                "&q=" + query +
                "&tbs=qdr:m" +
                "&api_key=" + API_KEY;
        
        // Add start parameter for pagination (page 1 = start 0, page 2 = start 10, etc.)
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
    
    private String getNextPageUrl(Map<String, Object> response) {
        if (response == null) return null;
        
        @SuppressWarnings("unchecked")
        Map<String, Object> serpApiPagination = (Map<String, Object>) response.get("serpapi_pagination");
        
        if (serpApiPagination != null && serpApiPagination.containsKey("next")) {
            String nextUrl = (String) serpApiPagination.get("next");
            // Add API key if not present
            if (nextUrl != null && !nextUrl.contains("api_key")) {
                nextUrl += "&api_key=" + API_KEY;
            }
            return nextUrl;
        }
        
        return null;
    }
    
    private List<JobResult> processSearchResults(Map<String, Object> response, String jobTitle, String location) {
        List<JobResult> results = new ArrayList<>();
        
        if (response != null && response.containsKey("organic_results")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> organicResults = (List<Map<String, Object>>) response.get("organic_results");
            
            if (organicResults != null) {
                for (Map<String, Object> item : organicResults) {
                    try {
                        String apiTitle = getStringValue(item, "title");
                        String link = getStringValue(item, "link");
                        String snippet = getStringValue(item, "snippet");
                        
                        String apiSource = getStringValue(item, "source");
                        if (apiSource == null || apiSource.isEmpty()) {
                            apiSource = getStringValue(item, "displayed_link");
                        }
                        if (apiSource == null || apiSource.isEmpty()) {
                            apiSource = extractDomainFromUrl(link);
                        }
                        
                        String dateString = getStringValue(item, "date");
                        LocalDate postedDate = parseJobDate(dateString);
                        LocalDate addedDate = LocalDate.now();
                        
                        if (apiTitle != null && !apiTitle.trim().isEmpty() && 
                            link != null && !link.trim().isEmpty()) {
                            results.add(new JobResult(apiSource, apiTitle, snippet, link, postedDate, addedDate, jobTitle, location));                            
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing individual result: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("No organic_results found in response");
            if (response != null && response.containsKey("error")) {
                System.err.println("API Error: " + response.get("error"));
            }
        }
        
        return results;
    }
    
    // Alternative method if you want to specify custom page limits
    public List<JobResult> searchJobsGoogle(String jobTitle, String location, int maxPages) {
        int originalMaxPages = this.MAX_PAGES;
        // You could make MAX_PAGES non-final and modify it, but for thread safety, 
        // it's better to pass it as a parameter through the recursive calls
        return searchJobsWithCustomLimit(jobTitle, location, 1, new ArrayList<>(), Math.min(maxPages, 10));
    }
    
    private List<JobResult> searchJobsWithCustomLimit(String jobTitle, String location, int currentPage, List<JobResult> allResults, int maxPages) {
        System.out.println("Searching page " + currentPage + " of max " + maxPages);
        
        if (currentPage > maxPages) {
            System.out.println("Reached maximum page limit of " + maxPages);
            return allResults;
        }
        
        try {
            String url = buildSearchUrl(jobTitle, location, currentPage);
            System.out.println("URL generated: " + url);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            List<JobResult> currentPageResults = processSearchResults(response, jobTitle, location);
            allResults.addAll(currentPageResults);
            
            System.out.println("Found " + currentPageResults.size() + " results on page " + currentPage);
            System.out.println("Total results so far: " + allResults.size());
            
            if (hasNextPage(response) && currentPage < maxPages) {
                return searchJobsWithCustomLimit(jobTitle, location, currentPage + 1, allResults, maxPages);
            }
            
            return allResults;
            
        } catch (RestClientException e) {
            System.err.println("Error calling SerpAPI on page " + currentPage + ": " + e.getMessage());
            return allResults;
        } catch (Exception e) {
            System.err.println("Unexpected error on page " + currentPage + ": " + e.getMessage());
            return allResults;
        }
    }
    
    // Keep all your existing helper methods unchanged
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }
    
    private String extractDomainFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "Unknown";
        }
        try {
            String domain = url.replaceFirst("^https?://", "");
            domain = domain.replaceFirst("^www\\.", "");
            int slashIndex = domain.indexOf('/');
            if (slashIndex > 0) {
                domain = domain.substring(0, slashIndex);
            }
            return domain;
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    private LocalDate parseJobDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return LocalDate.now();
        }
        
        dateString = dateString.trim();
        
        if (dateString.contains("ago")) {
            return parseRelativeDate(dateString);
        }
        
        return parseAbsoluteDate(dateString);
    }
    
    private LocalDate parseRelativeDate(String relativeDateString) {
        try {
            LocalDate currentDate = LocalDate.now();
            Pattern pattern = Pattern.compile("(\\d+)\\s+(hour|day|week|month)s?\\s+ago", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(relativeDateString);
            
            if (matcher.find()) {
                int amount = Integer.parseInt(matcher.group(1));
                String unit = matcher.group(2).toLowerCase();
                
                switch (unit) {
                    case "hour":
                        return amount >= 24 ? currentDate.minusDays(amount / 24) : currentDate;
                    case "day":
                        return currentDate.minusDays(amount);
                    case "week":
                        return currentDate.minusWeeks(amount);
                    case "month":
                        return currentDate.minusMonths(amount);
                    default:
                        return currentDate;
                }
            }
            return currentDate;
        } catch (Exception e) {
            System.err.println("Error parsing relative date: " + relativeDateString + " - " + e.getMessage());
            return LocalDate.now();
        }
    }
    
    private LocalDate parseAbsoluteDate(String absoluteDateString) {
        try {
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("MMM d, yyyy"),
                DateTimeFormatter.ofPattern("MMMM d, yyyy"),
                DateTimeFormatter.ofPattern("MMM dd, yyyy"),
                DateTimeFormatter.ofPattern("MMMM dd, yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
            };
            
            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(absoluteDateString, formatter);
                } catch (DateTimeParseException e) {
                    // Continue to next formatter
                }
            }
            
            System.err.println("Could not parse absolute date: " + absoluteDateString);
            return LocalDate.now();
            
        } catch (Exception e) {
            System.err.println("Error parsing absolute date: " + absoluteDateString + " - " + e.getMessage());
            return LocalDate.now();
        }
    }
}
