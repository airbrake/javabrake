package io.airbrake.javabrake;

public class RouteMetric extends Metrics {
    public static String HTTP_HANDLER = "http.handler";
    String method;
    String route;
    public int statusCode;
    public String contentType;

    public RouteMetric(String method, String route) {
        super();
        this.method = method;
        this.route = route;
        this.startSpan(HTTP_HANDLER, this.startTime);
    }

    public RouteMetric() {
    }

    public void end() {
        super.end();
        this.endSpan(HTTP_HANDLER, this.endTime);
    }

    public String getResponseType() {
        if (this.statusCode >= 500)
            return "5xx";
        if (this.statusCode >= 400)
            return "4xx";
        return this.contentType.split(";")[0].split("/")[-1];
    }
}
