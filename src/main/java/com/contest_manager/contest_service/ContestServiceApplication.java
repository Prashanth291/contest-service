package com.contest_manager.contest_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContestServiceApplication.class, args);
	}

}
