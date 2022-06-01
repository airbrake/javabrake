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
    WeatherService svc;

    @GetMapping("/date")
    public String date() {
        
        return svc.GetDate();
    }

    @GetMapping("/locations")
    public String locations() {
       
        return svc.GetLocations();
    }

    @GetMapping("/weather/{location}")
    public String weather(@PathVariable String location) {
    
        return svc.GetWeather(location);
      
    }

    @GetMapping("/weather1/{location}")
    public String weather1(@PathVariable String location) {
    
        return svc.GetWeather1(location);
      
    }
}
