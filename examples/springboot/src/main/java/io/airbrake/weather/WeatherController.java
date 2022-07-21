package io.airbrake.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;



@RestController("api")
@RequestMapping(value="/api")
public class WeatherController {

    @Autowired
    WeatherService weatherService;

    @GetMapping("/date")
    public String date() {
        
        return weatherService.getDate();
    }

    @GetMapping("/locations")
    public String locations() {
       
        return weatherService.getLocations();
    }

    @GetMapping("/weather/{location}")
    public String weather(@PathVariable String location) {
    
        return weatherService.getWeather(location);
      
    }

    @GetMapping("/weather1/{location}")
    public String weatherTryCatch(@PathVariable String location) {
    
        return weatherService.getWeatherTC(location);
      
    }
}
