package io.airbrake.javabrake;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import okhttp3.Response;

public class Routes {
    String environment;
    List<Object> routes = new ArrayList<>();

    transient String path;
    static transient String status = null;
    String date;

    Routes(String environment, List<Object> routes, String path) {
        this.environment = environment;
        this.routes = routes;
        this.path = path;
    }

    public Routes() {
    }

    public void notify(RouteMetric metrics) {
        try {
            Routes.status = null;
            if (!Notifier.config.performanceStats) {
                Routes.status = "performanceStats is disabled";
                return;
            }

            if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
                Notifier.config.environment = "production";
            }

            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());

            if (metrics.groups.size() == 1)
                RouteStats.notify(metrics, date);
            else    
                RouteBreakdowns.notify(metrics, date);

        } catch (Exception e) {
            Routes.status = e.toString();
        }
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
      
        RouteBreakdowns routeBreakdowns = new RouteBreakdowns(metrics.method, metrics.route, metrics.contentType,
                    date);
        Notifier.routesBreakdownList.add(routeBreakdowns);

        long msbr = metrics.endTime.getTime() - metrics.startTime.getTime();
        routeBreakdowns.addGroups(msbr, metrics.groups);

        RouteBreakDownTimerTask.start();
    }
}

class RouteTimerTask extends TimerTask {
    static Timer rTimer = new Timer();
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
            Routes routes = new Routes(Notifier.config.environment, Notifier.routeList, Constant.apmRoute);
            Notifier.routeList = new ArrayList<>();
            CompletableFuture<Response> future = new OkAsyncSender(Notifier.config).send(routes);
            future.whenComplete(
                    (value, exception) -> {
                        if (exception != null) {
                            Routes.status = exception.getMessage();
                        } else if (!value.isSuccessful()) {
                            Routes.status = value.message();
                        }
                    });
        }
    }

    public static void stop() {
        rTimer.cancel();
    }
}

class RouteBreakDownTimerTask extends TimerTask {
    static Timer brTimer = new Timer();
    static boolean isStartedBrakedown = false;

    public static void start() {
        if (!isStartedBrakedown) {
            isStartedBrakedown = true;
            brTimer.schedule(new RouteBreakDownTimerTask(), 0, Metrics.FLUSH_PERIOD * 1000);
        }
    }

    @Override
    public void run() {
        isStartedBrakedown = true;

        if (Notifier.routesBreakdownList.size() > 0) {
            Routes routes = new Routes(Notifier.config.environment, Notifier.routesBreakdownList,
                    Constant.apmRouteBreakDown);
            Notifier.routesBreakdownList = new ArrayList<>();
            CompletableFuture<Response> future = new OkAsyncSender(Notifier.config).send(routes);

            future.whenComplete(
                    (value, exception) -> {
                        if (exception != null) {
                            Routes.status = exception.getMessage();
                        } else if (!value.isSuccessful()) {
                            Routes.status = value.message();
                        }
                    });
        }
    }

    public static void stop() {
        brTimer.cancel();
    }
}
