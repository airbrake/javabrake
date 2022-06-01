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
		
		assertNotNull(weatherService.GetDate());
	}

	@Test
	void getLocations() {
		
		assertNotNull(weatherService.GetLocations());
	}

	@Test
	void getWeather() {
		
		assertNotNull(weatherService.GetWeather("austin"));
	}

	@Test
	public void checkIfWeatherIsNotAvailable() {
    Exception exception = assertThrows(org.springframework.web.client.HttpClientErrorException.class, () -> {
        weatherService.GetWeather("boston");
    });

    String expectedMessage = "404 Not Found:";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
}
}
