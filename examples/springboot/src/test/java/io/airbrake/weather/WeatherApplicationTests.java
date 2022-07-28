package io.airbrake.weather;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class WeatherApplicationTests {

	@Autowired
	WeatherService weatherService;
	
	
	@Test
	void getDate() {
		
		assertNotNull(weatherService.getDate());
	}

	@Test
	void getLocations() {
		
		assertNotNull(weatherService.getLocations());
	}

	@Test
	void getWeather() {
		
		assertNotNull(weatherService.getWeather("austin"));
	}

	@Test
	public void checkIfWeatherIsNotAvailable() {
    Exception exception = assertThrows(org.springframework.web.client.HttpClientErrorException.class, () -> {
        weatherService.getWeather("boston");
    });

    String expectedMessage = "404 Not Found:";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
}
}
