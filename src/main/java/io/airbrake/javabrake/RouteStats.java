package io.airbrake.javabrake;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import okhttp3.Response;

public class RouteStats extends TdigestStat {
    String method;
    String route;
    int statusCode;
    String time;

    static transient Config config;
    static transient Throwable exception;

    public RouteStats(String method, String route, int statusCode, String time) {
        this.method = method;
        this.route = route;
        this.statusCode = statusCode;
        this.time = time;
    }

    public static void notify(RouteMetric metrics, Config config) {
        try {

            RouteStats.config = config;
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());

            RouteStats routeStats = new RouteStats(metrics.method, metrics.route, metrics.statusCode, date);
            Notifier.routes.add(routeStats);

            long ms = metrics.endTime.getTime() - metrics.startTime.getTime();
            routeStats.add(ms);
            routeStats.tdigest = routeStats.getData();
            // routeStats.tdigest = "AAAAAkA0AAAAAAAAAAAAAUdqYAAB";

            if (!RouteTimerTask.hasStarted)
                RouteTimerTask.start();
        } catch (Exception e) {
            RouteStats.exception = e;
        }
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
        // TODO Auto-generated method stub

        hasStarted = true;
        Routes routes = new Routes(RouteStats.config.environment, Notifier.routes);
        Notifier.routes = new ArrayList<>();
        CompletableFuture<Response> future = new OkAsyncSender(RouteStats.config).sendRouteStats(routes);
        future.whenComplete(
                (value, exception) -> {
                    if (exception != null) {
                        RouteStats.exception = exception;
                        Notifier.routes.addAll(routes.routes);
                    }
                });
    }

    public static void stop() {
        rTimer.cancel();
    }
}

class Routes {
    String environment;
    List<Object> routes = new ArrayList<>();

    public Routes(String environment, List<Object> routes) {
        this.environment = environment;
        this.routes = routes;

    }
}