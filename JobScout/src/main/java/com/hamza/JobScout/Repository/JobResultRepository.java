package com.hamza.JobScout.Repository;


import com.hamza.JobScout.Model.JobResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobResultRepository extends JpaRepository<JobResult, Long> {

    // Find by company
    List<JobResult> findByCompany(String company);

    // Find by job title
    List<JobResult> findByJobTitle(String jobTitle);

    // Find by location
    List<JobResult> findByLocation(String location);

    // Find by company and location
    List<JobResult> findByCompanyAndLocation(String company, String location);

    // Find jobs posted after a given date
    List<JobResult> findByPostedDateAfter(LocalDate date);

    // Find jobs added on a specific date
    List<JobResult> findByAddedDate(LocalDate date);

    // Avoid duplicates: check if a job with same link already exists
    boolean existsByLink(String link);
    
    List<JobResult> findAll();
    
    List<JobResult> findByJobTitleAndLocationOrderByAddedDateDesc(String jobTitle, String location);

}