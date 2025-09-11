package com.hamza.JobScout.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hamza.JobScout.entity.JobResult;
import com.hamza.JobScout.service.JobSearchService;

@RestController
@RequestMapping("/searchJob")
public class JobSearchController {
	private final JobSearchService jobSearchService;
	
	
    public JobSearchController(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }
    
    
    @CrossOrigin(origins = "*")
    @GetMapping
    public ResponseEntity<List<JobResult>> searchJobsGoogle(
        @RequestParam String jobTitle,
        @RequestParam String location) {
        
        List<JobResult> results = jobSearchService.searchJobs(jobTitle, location);
        return ResponseEntity.ok(results);
    }

}
