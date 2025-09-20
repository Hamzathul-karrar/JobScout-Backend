package com.hamza.JobScout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobScoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobScoutApplication.class, args);
		System.out.println("Backend started ");
	}

}
