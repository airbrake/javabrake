package io.airbrake.weather;

import io.airbrake.javabrake.Notifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionContoller {
    
    @Autowired
    Notifier notifier;

    @ExceptionHandler(Exception.class)
    public void ExceptionHandling(Exception e)
    {
        notifier.report(e);
    }  
}
