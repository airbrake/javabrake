package io.airbrake.javabrake;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;
import okhttp3.Response;

public class Queries {
    String environment;
    List<Object> queries;

    transient String url;
    static transient String status = null;
    
    Queries(String environment, List<Object> queries, String url) {
        this.environment = environment;
        this.queries = queries;
        this.url = url;
    }

    public Queries() {
    }

    public void notify(@NotNull String method, @NotNull String route, @NotNull String query, @NotNull Date startTime,
            @NotNull Date endTime, @NotNull String function,
            @NotNull String file, @NotNull int line) {
                status = null;
        if (!Notifier.config.performanceStats) {
            status = "performanceStats is disabled";
            return;
        }

        if (!Notifier.config.queryStats) {
            status = "queryStats is disabled";
            return;
        }

        if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
            Notifier.config.environment = "production";
        }

        try {
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(startTime);
            QueryStats queryStats = new QueryStats(method, route, query, date, function, file, line);
            Notifier.queries.add(queryStats);

            long ms = endTime.getTime() - startTime.getTime();
            queryStats.add(ms);
            queryStats.tdigest = queryStats.getData();
            QueryTimerTask.start();
        } catch (Exception e) {
            status = e.getMessage();
        }
        return;
    }

    public void notify(@NotNull String method, @NotNull String route, @NotNull String query, @NotNull Date startTime,
            @NotNull Date endTime) {
                status = null;
        if (!Notifier.config.performanceStats) {
            status = "performanceStats is disabled";
            return;
        }

        if (!Notifier.config.queryStats) {
            status = "queryStats is disabled";
            return;
        }

        if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
            Notifier.config.environment = "production";
        }

        try {
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(startTime);
            QueryStats queryStats = new QueryStats(method, route, query, date);
            Notifier.queries.add(queryStats);

            long ms = endTime.getTime() - startTime.getTime();
            queryStats.add(ms);
            queryStats.tdigest = queryStats.getData();
            QueryTimerTask.start();
        } catch (Exception e) {
            status = e.getMessage();
        }
        return;
    }
}

class QueryStats extends TdigestStat {

    String method;
    String route;
    String query;
    String time;
    String function;
    String file;
    int line;

    QueryStats(String method, String route, String query, String time) {
        this.method = method;
        this.route = route;
        this.query = query;
        this.time = time;
    }

    public QueryStats(String method, String route, String query, String time, String function, String file, int line) {
        this.method = method;
        this.route = route;
        this.query = query;
        this.time = time;
        this.function = function;
        this.file = file;
        this.line = line;
    }
}

class QueryTimerTask extends TimerTask {
    static Timer rTimer = new Timer();
    private static boolean hasStarted = false;

    public static void start() {
        if (!hasStarted) {
            hasStarted = true;
            rTimer.schedule(new QueryTimerTask(), 0, Metrics.FLUSH_PERIOD * 1000);
        }
    }

    @Override
    public void run() {
        hasStarted = true;

        if (Notifier.queries.size() > 0) {
            Queries queries = new Queries(Notifier.config.environment, Notifier.queries, Constant.apmQuery);
            Notifier.queries = new ArrayList<>();
            CompletableFuture<Response> future = new OkAsyncSender(Notifier.config).send(queries);

            future.whenComplete(
                    (value, exception) -> {
                        if (exception != null) {
                            Queries.status = exception.getMessage();
                        } else if(!value.isSuccessful())  
                        {
                            Queries.status = value.message();
                        }  
                    });
        }
    }

    public static void stop() {
        rTimer.cancel();
    }
}
