package io.airbrake.javabrake;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import okhttp3.Response;

public class Queues {

    String environment;
    List<Object> queues;
    static transient String status = null;

    Queues(String environment, List<Object> queues) {
        this.environment = environment;
        this.queues = queues;
    }

    Queues() {
    }

    public void notify(QueueMetric metrics) {
        Queues.status = null;
        if (!Notifier.config.performanceStats) {
            Queues.status = "performanceStats is disabled";
            return;
        }

        if (!Notifier.config.queueStats) {
            Queues.status = "queueStats is disabled";
            return;
        }

        if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
            Notifier.config.environment = "production";
        }

        String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(metrics.startTime);

        QueueStats queueStats = new QueueStats(metrics.queue, date);
        Notifier.queueList.add(queueStats);

        long ms = metrics.endTime.getTime() - metrics.startTime.getTime();
        queueStats.addGroups(ms, metrics.groups);

        QueueTimerTask.start();

    }
}

class QueueStats extends TdigestStatGroup {

    String queue;
    String time;

    public QueueStats(String queue, String time) {
        this.queue = queue;
        this.time = time;
    }
}

class QueueTimerTask extends TimerTask {
    static Timer rTimer = new Timer(true);
    private static boolean hasStarted = false;

    public static void start() {
        if (!hasStarted) {
            hasStarted = true;
            rTimer.schedule(new QueueTimerTask(), 0, Metrics.FLUSH_PERIOD * 1000);
        }
    }

    @Override
    public void run() {

        hasStarted = true;

        if (Notifier.queueList.size() > 0) {

            Queues queues = new Queues(Notifier.config.environment, Notifier.queueList);
            Notifier.queueList = new ArrayList<>();
            CompletableFuture<Response> future = new OkAsyncSender(Notifier.config).send(OkSender.gson.toJson(queues),
                    Constant.apmQueue);

            future.whenComplete(
                    (value, exception) -> {
                        if (exception != null) {
                            Queues.status = exception.getMessage();
                        }
                    });
        }
    }

    public static void stop() {
        rTimer.cancel();
    }
}
