package io.airbrake.javabrake;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

public class Queries {
    String environment;
    List<Object> queries;

    static transient String status = null;

    Queries(String environment, List<Object> queries) {
        this.environment = environment;
        this.queries = queries;
    }

    public Queries() {
    }

    public void notify(@NotNull String method, @NotNull String route, @NotNull String query, @NotNull Date startTime,
            @NotNull Date endTime, @NotNull String function,
            @NotNull String file, @NotNull int line) {
        Queries.status = null;
        if (!Notifier.config.performanceStats) {
            Queries.status = "performanceStats is disabled";
            return;
        }

        if (!Notifier.config.queryStats) {
            Queries.status = "queryStats is disabled";
            return;
        }

        if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
            Notifier.config.environment = "production";
        }

            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(startTime);
            QueryStats queryStats = new QueryStats(method, route, query, date, function, file, line);
            Notifier.queryList.add(queryStats);

            long ms = endTime.getTime() - startTime.getTime();
            queryStats.add(ms);

            QueryTimerTask.start();
    }

    public void notify(@NotNull String method, @NotNull String route, @NotNull String query, @NotNull Date startTime,
            @NotNull Date endTime) {
        Queries.status = null;
        if (!Notifier.config.performanceStats) {
            Queries.status = "performanceStats is disabled";
            return;
        }

        if (!Notifier.config.queryStats) {
            Queries.status = "queryStats is disabled";
            return;
        }

        if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
            Notifier.config.environment = "production";
        }

        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(startTime);
        QueryStats queryStats = new QueryStats(method, route, query, date);
        Notifier.queryList.add(queryStats);

        long ms = endTime.getTime() - startTime.getTime();
        queryStats.add(ms);
        queryStats.tdigest = queryStats.getData();
        QueryTimerTask.start();
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
    static Timer rTimer = new Timer(true);
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

        if (Notifier.queryList.size() > 0) {
            Queries queries = new Queries(Notifier.config.environment, Notifier.queryList);
            Notifier.queryList = new ArrayList<>();
            CompletableFuture<ApmResponse> future = new OkAsyncSender(Notifier.config).send(OkSender.gson.toJson(queries),
                    Constant.apmQuery);

            future.whenComplete(
                    (value, exception) -> {
                        if (exception != null) {
                            Queries.status = exception.getMessage();
                        } else if (value != null) {
                            Queries.status = value.message;

                            if (Notifier.config.backlogEnabled && value != null && Constant.getStatusCodeCriteriaForBacklog().contains(value.code)) {
                                    BackLog.add(new PayLoad(OkSender.gson.toJson(queries), Constant.apmQuery, 0));
                            }
                        }
                    });
        }
    }

    public static void stop() {
        rTimer.cancel();
    }
}
