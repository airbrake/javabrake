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
    transient String url;
    static transient String status = null;

    Queues(String environment, List<Object> queues, String url) {
        this.environment = environment;
        this.queues = queues;
        this.url = url;
    }

    Queues() {
    }

    public void notify(QueueMetric metrics) {

        if (!Notifier.config.performanceStats) {
            status = "performanceStats is disabled";
            return;

        }

        if (!Notifier.config.queueStats) {
            status = "queueStats is disabled";
            return;
        }

        if (Notifier.config.environment == null || Notifier.config.environment.equals("")) {
            Notifier.config.environment = "production";
        }

        try {
            String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(metrics.startTime);

            QueueStats queueStats = new QueueStats(metrics.queue, date);
            Notifier.queueList.add(queueStats);

            long ms = metrics.endTime.getTime() - metrics.startTime.getTime();
            queueStats.addGroups(ms, metrics.groups);

            QueueTimerTask.start();
        } catch (Exception e) {
            status = e.getMessage();
        }
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
    static Timer rTimer = new Timer();
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

            Queues queues = new Queues(Notifier.config.environment, Notifier.queueList, Constant.apmQueue);
            Notifier.queueList = new ArrayList<>();
            CompletableFuture<Response> future = new OkAsyncSender(Notifier.config).send(queues);

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
