package io.airbrake.weather;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import io.airbrake.javabrake.Notifier;

@Service
public class WeatherService {

  
    @Value("${airbrake.baseurl}")
    private String baseUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    Notifier notifier; 

    public String getDate() {
        String url = baseUrl+"/date";
        String resp = this.restTemplate.getForObject(url, String.class);
        long l = Long.parseLong(resp.trim());
        Date d = new Date(l * 1000);
        
        return d.toString();
    }

    public String getLocations() {
        String url = baseUrl+"/locations";
        String resp = this.restTemplate.getForObject(url, String.class);
        
        return resp;
    }

    //Api is to check location is available or not? 
    //If not then it will given an exception. Exception will get reported to airbrake protal through GlobalExceptionController.
    public String getWeather(String location)  {
        String url = baseUrl+"/weather/{location}";
        ResponseEntity<String> resp = new ResponseEntity<>(HttpStatus.OK);
        resp = this.restTemplate.getForEntity(url, String.class, location);
    
        return resp.getBody();
    }

     //Using try-catch exception will get reported to airbrake protal
    public String getWeatherTrtCatch(String location)  {
        String url = baseUrl+"/weather/{location}";
        ResponseEntity<String> resp = new ResponseEntity<>(HttpStatus.OK);
        try{
        resp = this.restTemplate.getForEntity(url, String.class, location);
        }
        catch(Exception e){
            notifier.report(e);
        }
        return resp.getBody();
    }
    
}
