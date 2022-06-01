package io.airbrake.weather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import io.airbrake.javabrake.Config;
import io.airbrake.javabrake.Notifier;

@SpringBootApplication
public class WeatherApplication {

	@Value("${airbrake.project.id}")
    private int projectId;

    @Value("${airbrake.project.key}")
    private String projectKey;

	static Notifier notifier;

	
	public static void main(String[] args) {
		SpringApplication.run(WeatherApplication.class, args);
	}

@Bean
public RestTemplate restTemplate() {
    	return new RestTemplate();
	}

@Bean
public Notifier getNotifier() {
		Config config = new Config();
		config.projectId = projectId;
		config.projectKey = projectKey;
		notifier = new Notifier(config);
	    return notifier;
	}
}