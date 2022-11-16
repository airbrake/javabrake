package io.airbrake.javabrake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constant {

    protected static String apmRoute = "routes-stats";
    protected static String apmRouteBreakDown = "routes-breakdowns";
    protected static String apmQuery = "queries-stats";
    protected static String apmQueue = "queues-stats";
    protected static int maxRetryAttempt = 1;
    private static Integer[] statusCode = { 404, 408, 409, 410, 500, 502, 504};
    protected static int BACKLOG_FLUSH_PERIOD = 60;
    protected static List<Integer> getStatusCodeCriteriaForBacklog() {
        List<Integer> statusCodeCriteriaForBacklog = new ArrayList<>();
        if (statusCodeCriteriaForBacklog.size() != statusCode.length)
            statusCodeCriteriaForBacklog = Arrays.asList(statusCode);
        return statusCodeCriteriaForBacklog;
    }

}
