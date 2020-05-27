package com.jg.springintegration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;

@EnableIntegration
@SpringBootApplication
public class SpringIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringIntegrationApplication.class, args);
	}

}
