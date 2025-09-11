package com.hamza.JobScout.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_results")
public class JobResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String company;     // Job title from API
    private String title;       // Company domain / Source
    private String description; // Snippet
    private String link;        // Job URL

    private LocalDate postedDate;  // From SerpAPI result
    private LocalDate addedDate; // When inserted into DB
    
    private String jobTitle;     // e.g. Software Engineer
    private String location;    // e.g. Bangalore

    // Default constructor
    public JobResult() {}

    // Constructor with fields
    public JobResult(String company, String title, String description, String link,
    		LocalDate postedDate, LocalDate addedDate,
                     String jobTitle, String location) {
    	this.company = company;
        this.title = title;
        this.description = description;
        this.link = link;
        this.setPostedDate(postedDate);
        this.setAddedDate(addedDate);
        this.jobTitle = jobTitle;
        this.location = location;
    }

    
    // getters & setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}


	public String getJobType() {
		return jobTitle;
	}

	public void setJobType(String jobType) {
		this.jobTitle = jobType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public LocalDate getPostedDate() {
		return postedDate;
	}

	public void setPostedDate(LocalDate postedDate) {
		this.postedDate = postedDate;
	}

	public LocalDate getAddedDate() {
		return addedDate;
	}

	public void setAddedDate(LocalDate addedDate) {
		this.addedDate = addedDate;
	}

	
}
