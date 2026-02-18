package com.example.distributed_api_demo_backend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SpringBootApplication
@Slf4j
public class DistributedApiDemoBackendApplication {

	public static void main(String[] args) {
		log.info("Starting Oracle Hospitality Distribution API Demo Backend...");
		SpringApplication.run(DistributedApiDemoBackendApplication.class, args);
		log.info("Application started successfully!");
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return mapper;
	}
}
