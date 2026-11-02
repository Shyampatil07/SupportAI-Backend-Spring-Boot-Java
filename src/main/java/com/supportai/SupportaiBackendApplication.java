package com.supportai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.supportai.config.AiProperties;

@EnableScheduling
@EnableConfigurationProperties(AiProperties.class)
@SpringBootApplication
public class SupportaiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SupportaiBackendApplication.class, args);
		System.err.println("SupportAI Backend Application is running...");
	}

}
