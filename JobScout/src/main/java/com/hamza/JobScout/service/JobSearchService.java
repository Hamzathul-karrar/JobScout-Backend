package com.hamza.JobScout.service;

import com.hamza.JobScout.entity.JobResult;
import com.hamza.JobScout.repository.JobResultRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JobSearchService {

    private final JobResultRepository jobResultRepository;
    private final SerpApiService serpApiService;
    private final JobResultProcessor jobResultProcessor;
    private static final Logger logger = LoggerFactory.getLogger(JobSearchService.class);


    public JobSearchService(JobResultRepository jobResultRepository,
                           SerpApiService serpApiService,
                           JobResultProcessor jobResultProcessor) {
        this.jobResultRepository = jobResultRepository;
        this.serpApiService = serpApiService;
        this.jobResultProcessor = jobResultProcessor;
    }

    // Modified to accept userId parameter
    public List<JobResult> searchJobs(String jobTitle, String location, Long userId) {
        jobTitle = jobTitle.toLowerCase().trim();
        location = location.toLowerCase().trim();
        logger.info("Starting job search for '{}' in '{}'", jobTitle, location);

        // Step 1: Query DB for existing jobs
        List<JobResult> existingJobs = jobResultRepository.findByJobTitleAndLocationOrderByAddedDateDesc(
                jobTitle, location);

        // Step 2: Determine if we need to call SerpAPI and with what time filter
        String timeFilter = determineTimeFilter(existingJobs);
        if (timeFilter == null) {
            // Jobs are fresh (from today), return existing jobs
            logger.info("Returning {} fresh jobs from database", existingJobs.size());
            return existingJobs;
        }

        // Step 3: Call SerpAPI with determined time filter and userId
        logger.info("Calling SerpAPI with time filter: {}", timeFilter);
        List<Map<String, Object>> serpApiResponses = serpApiService.searchJobs(jobTitle, location, timeFilter, userId);

        // Step 4: Process and save new jobs (deduplication handled in processor)
        List<JobResult> newJobs = jobResultProcessor.processAndSaveResults(serpApiResponses, jobTitle, location);

        // Step 5: Combine existing and new jobs, then sort by addedDate (desc)
        List<JobResult> allJobs = Stream.concat(existingJobs.stream(), newJobs.stream())
                .sorted((job1, job2) -> job2.getAddedDate().compareTo(job1.getAddedDate())) // Sort desc
                .collect(Collectors.toList());

        logger.info("Returning {} total jobs ({} new)", allJobs.size(), newJobs.size());
        return allJobs;
    }

    /**
     * Determines the appropriate time filter based on existing jobs
     * @param existingJobs List of existing jobs from database
     * @return Time filter string for SerpAPI, or null if jobs are fresh
     */
    private String determineTimeFilter(List<JobResult> existingJobs) {
        if (existingJobs.isEmpty()) {
            // No existing jobs, search for jobs from past week
            logger.info("No existing jobs found, searching past month");
            return "qdr:m";
        }

        // Get the most recent job's added date
        LocalDate mostRecentDate = existingJobs.get(0).getAddedDate();
        LocalDate today = LocalDate.now();
        long daysBetween = ChronoUnit.DAYS.between(mostRecentDate, today);

        logger.info("Most recent job was added {} days ago", daysBetween);
        if (daysBetween == 0) {
            // Jobs are from today, no need to call API
            return null;
        } else if (daysBetween == 1) {
            // Jobs are 1 day old, search past 24 hours
            return "qdr:d";
        } else if (daysBetween <= 7) {
            // Jobs are up to 1 week old, search past week
            return "qdr:w";
        } else if (daysBetween <= 30) {
            // Jobs are up to 1 month old, search past month
            return "qdr:m";
        } else {
            // Jobs are very old, search past week for fresh results
            return "qdr:m";
        }
    }
}
