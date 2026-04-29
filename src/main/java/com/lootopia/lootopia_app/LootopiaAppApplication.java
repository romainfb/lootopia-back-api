package com.lootopia.lootopia_app;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.stream.Collectors;

@SpringBootApplication
@EnableMethodSecurity
@EnableScheduling
public class LootopiaAppApplication {

	private static final Logger logger = LoggerFactory.getLogger(LootopiaAppApplication.class);

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(LootopiaAppApplication.class);

		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});

		app.setListeners(app.getListeners()
				.stream()
				.filter(listener -> !(listener instanceof ConditionEvaluationReportLoggingListener))
				.collect(Collectors.toList()));

		app.run(args);

		String port = "8080";
		String contextPath = "/api";

		logger.info("Application is running on http://localhost:" + port + contextPath);
	}
}
