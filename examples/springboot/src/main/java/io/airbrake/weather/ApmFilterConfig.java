package io.airbrake.weather;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.airbrake.javabrake.Notifier;
import io.airbrake.javabrake.RouteMetric;

@Component
public class ApmFilterConfig implements Filter {

    @Autowired
    Notifier notifier;

     static ConcurrentHashMap<String, RouteMetric> requestList = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterchain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String sessionId = req.getSession().getId();
        if(!requestList.contains(sessionId))
        requestList.put(sessionId,  new RouteMetric(req.getMethod(), req.getRequestURI()));

        filterchain.doFilter(request, response);

        HttpServletResponse res = ((HttpServletResponse) response);
        RouteMetric routeMetric = requestList.get(sessionId);
        requestList.remove(sessionId);
        routeMetric.statusCode = res.getStatus();
        routeMetric.end();
        notifier.routes.notify(routeMetric);
    }

}
