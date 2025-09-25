package com.hamza.JobScout.service;

import com.hamza.JobScout.repository.JobResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class JobCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(JobCleanupService.class);
    private static final int RETENTION_DAYS = 30;
    
    @Autowired
    private JobResultRepository jobResultRepository;
    
    // Run daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldJobs() {
        LocalDate cutoffDate = LocalDate.now().minusDays(RETENTION_DAYS);
        
        try {
            // Count records to be deleted for logging
            long recordsToDelete = jobResultRepository.countByAddedDateBefore(cutoffDate);
            
            if (recordsToDelete > 0) {
                logger.info("Starting cleanup: {} job records older than {} days will be deleted", 
                           recordsToDelete, RETENTION_DAYS);
                
                // Delete old records
                jobResultRepository.deleteByAddedDateBefore(cutoffDate);
                
                logger.info("Cleanup completed: {} job records deleted successfully", recordsToDelete);
            } else {
                logger.info("No job records older than {} days found for cleanup", RETENTION_DAYS);
            }
            
        } catch (Exception e) {
            logger.error("Error during job cleanup: {}", e.getMessage(), e);
        }
    }
}
