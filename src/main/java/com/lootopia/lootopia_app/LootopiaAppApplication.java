package com.lootopia.lootopia_app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class LootopiaAppApplication {

	private static final Logger logger = LoggerFactory.getLogger(LootopiaAppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LootopiaAppApplication.class, args);

		String port = "8080";
		String contextPath = "/api";

		logger.info("Application is running on http://localhost:" + port + contextPath);
	}
}
