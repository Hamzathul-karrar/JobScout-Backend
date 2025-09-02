package com.hamza.JobScout.Service;

import com.hamza.JobScout.Model.JobResult;
import com.hamza.JobScout.Repository.JobResultRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JobResultProcessor {
    
    private final JobResultRepository jobResultRepository;
    
    public JobResultProcessor(JobResultRepository jobResultRepository) {
        this.jobResultRepository = jobResultRepository;
    }
    
    public List<JobResult> processAndSaveResults(List<Map<String, Object>> serpApiResponses, 
            String jobTitle, String location) {
        
        List<JobResult> allNewJobs = new ArrayList<>();
        
        for (Map<String, Object> response : serpApiResponses) {
            List<JobResult> pageResults = processResponse(response, jobTitle, location);
            allNewJobs.addAll(pageResults);
        }
        
        // Bulk save all new jobs at once
        if (!allNewJobs.isEmpty()) {
            jobResultRepository.saveAll(allNewJobs);
            System.out.println("Saved " + allNewJobs.size() + " new jobs to database");
        }
        
        return allNewJobs;
    }
    
    private List<JobResult> processResponse(Map<String, Object> response, String jobTitle, String location) {
        List<JobResult> newJobs = new ArrayList<>();
        
        if (response == null || !response.containsKey("organic_results")) {
            return newJobs;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> organicResults = (List<Map<String, Object>>) response.get("organic_results");
        
        if (organicResults == null) {
            return newJobs;
        }
        
        for (Map<String, Object> item : organicResults) {
            try {
                JobResult job = parseJobResult(item, jobTitle, location);
                if (job != null && !jobResultRepository.existsByLink(job.getLink())) {
                    newJobs.add(job);
                }
            } catch (Exception e) {
                System.err.println("Error parsing individual result: " + e.getMessage());
            }
        }
        
        return newJobs;
    }
    
    private JobResult parseJobResult(Map<String, Object> item, String searchJobTitle, String searchLocation) {
        String title = getStringValue(item, "title");
        String link = getStringValue(item, "link");
        String snippet = getStringValue(item, "snippet");
        String source = getStringValue(item, "source");
        
        // Fallback for source
        if (source == null || source.isEmpty()) {
            source = getStringValue(item, "displayed_link");
        }
        if (source == null || source.isEmpty()) {
            source = extractDomainFromUrl(link);
        }
        
        // Parse date
        String dateString = getStringValue(item, "date");
        LocalDate postedDate = parseJobDate(dateString);
        LocalDate addedDate = LocalDate.now();
        
        // Validate required fields
        if (title == null || title.trim().isEmpty() || link == null || link.trim().isEmpty()) {
            return null;
        }
        
        return new JobResult(
            source,           // company
            title,            // title  
            snippet,          // description
            link,             // url
            postedDate,       // postedDate
            addedDate,        // addedDate
            searchJobTitle,   // searchTerm
            searchLocation    // location
        );
    }
    
    // Helper methods from original code
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return (value == null) ? null : value.toString().trim();
    }
    
    private String extractDomainFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "Unknown";
        }
        try {
            String domain = url.replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
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
                }
            }
            return currentDate;
        } catch (Exception e) {
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
                } catch (DateTimeParseException ignored) {
                }
            }
            return LocalDate.now();
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}
