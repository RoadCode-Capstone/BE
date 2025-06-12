package com.capstone2025.roadcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RoadcodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoadcodeApplication.class, args);
	}

}
