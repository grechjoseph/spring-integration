package com.jg.multiplespringintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@EnableIntegration
@SpringBootApplication
public class MultipleSpringIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultipleSpringIntegrationApplication.class, args);
	}

}
