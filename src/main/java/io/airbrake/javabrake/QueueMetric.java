package io.airbrake.javabrake;

public class QueueMetric extends Metrics{
    public static String QUEUE_HANDLER = "queue.handler";
    String queue;

    public QueueMetric(String queue) {
        super();
        this.queue = queue;
        this.startSpan(QUEUE_HANDLER, this.startTime);
    }

    public void end() {
        super.end();
        this.endSpan(QUEUE_HANDLER, this.endTime);
    }
}
