package io.airbrake.javabrake;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class Routes {
    String environment;
    List<Object> routes = new ArrayList<>();

    static transient String status = null;
    String date;

    Routes(String environment, List<Object> routes) {
        this.environment = environment;
        this.routes = routes;
    }

    Routes() {
    }

    public void notify(RouteMetric metrics) {
        Routes.status = null;
        if (!Notifier.config.performanceStats) {
            Routes.status = "performanceStats is disabled";
            return;
        }

        if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
            Notifier.config.environment = "production";
        }

        date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());

        RouteStats.notify(metrics, date);
        RouteBreakdowns.notify(metrics, date);
    }
}

class RouteStats extends TdigestStat {
    String method;
    String route;
    int statusCode;
    String time;

    RouteStats(String method, String route, int statusCode, String time) {
        this.method = method;
        this.route = route;
        this.statusCode = statusCode;
        this.time = time;
    }

    static void notify(RouteMetric metrics, String date) {
        RouteStats routeStats = new RouteStats(metrics.method, metrics.route, metrics.statusCode, date);
        Notifier.routeList.add(routeStats);

        long ms = metrics.endTime.getTime() - metrics.startTime.getTime();
        routeStats.add(ms);

        RouteTimerTask.start();
    }

}

class RouteBreakdowns extends TdigestStatGroup {

    String method;
    String route;
    String responseType;
    String time;

    public RouteBreakdowns(String method, String route, String responseType, String time) {
        this.method = method;
        this.route = route;
        this.responseType = responseType;
        this.time = time;
    }

    static void notify(RouteMetric metrics, String date) {

        if (metrics.groups.size() > 1) {
            RouteBreakdowns routeBreakdowns = new RouteBreakdowns(metrics.method, metrics.route, metrics.contentType,
                    date);
            Notifier.routesBreakdownList.add(routeBreakdowns);

            long msbr = metrics.endTime.getTime() - metrics.startTime.getTime();
            routeBreakdowns.addGroups(msbr, metrics.groups);

            RouteBreakDownTimerTask.start();
        }
    }
}

class RouteTimerTask extends TimerTask {
    static Timer rTimer = new Timer(true);
    static boolean hasStarted = false;

    public static void start() {
        if (!hasStarted) {
            hasStarted = true;
            rTimer.schedule(new RouteTimerTask(), 0, Metrics.FLUSH_PERIOD * 1000);
        }
    }

    @Override
    public void run() {
        hasStarted = true;

        if (Notifier.routeList.size() > 0) {
            Routes routes = new Routes(Notifier.config.environment, Notifier.routeList);
            Notifier.routeList = new ArrayList<>();
            CompletableFuture<ApmResponse> future = new OkAsyncSender(Notifier.config).send(OkSender.gson.toJson(routes),
                    Constant.apmRoute);
            future.whenComplete(
                    (value, exception) -> {
                        if (exception != null) {
                            Routes.status = exception.getMessage();
                        } else if (value != null) {
                            Routes.status = value.message;

                            if (Notifier.config.backlogEnabled && value!=null && Constant.failureCodeList().contains(value.code)) {
                                    APMBackLog.add(new PayLoad(OkSender.gson.toJson(routes), Constant.apmRoute, 0));
                            }
                        }
                    });
        }
    }

    public static void stop() {
        rTimer.cancel();
    }
}

class RouteBreakDownTimerTask extends TimerTask {
    static Timer brTimer = new Timer(true);
    static boolean isStartedBreakdown = false;

    public static void start() {
        if (!isStartedBreakdown) {
            isStartedBreakdown = true;
            brTimer.schedule(new RouteBreakDownTimerTask(), 0, Metrics.FLUSH_PERIOD * 1000);
        }
    }

    @Override
    public void run() {
        isStartedBreakdown = true;

        if (Notifier.routesBreakdownList.size() > 0) {
            Routes routes = new Routes(Notifier.config.environment, Notifier.routesBreakdownList);
            Notifier.routesBreakdownList = new ArrayList<>();
            CompletableFuture<ApmResponse> future = new OkAsyncSender(Notifier.config).send(OkSender.gson.toJson(routes),
                    Constant.apmRouteBreakDown);

            future.whenComplete(
                    (value, exception) -> {
                        if (exception != null) {
                            Routes.status = exception.getMessage();
                        } else if (value != null) {
                            Routes.status = value.message;

                            if (Notifier.config.backlogEnabled && value!=null && Constant.failureCodeList().contains(value.code)) {
                                    APMBackLog.add(new PayLoad(OkSender.gson.toJson(routes), Constant.apmRouteBreakDown, 0));
                            }
                        }
                    });
        }
    }

    public static void stop() {
        brTimer.cancel();
    }
}
