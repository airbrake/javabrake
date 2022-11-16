package io.airbrake.javabrake;

import java.util.Date;
import java.util.concurrent.Future;

/** Airbrake is a proxy class for default Notifier. */
public class Airbrake {
  static Notifier notifier;

  public static void setNotifier(Notifier notifier) {
    Airbrake.notifier = notifier;
  }

  public static Future<Notice> report(Throwable e) {
    return notifier.report(e);
  }

  public static Future<Notice> send(Notice notice) {
    return notifier.send(notice);
  }

  public static Notice sendSync(Notice notice) {
    return notifier.sendSync(notice);
  }

  public static Notice buildNotice(Throwable e) {
    return notifier.buildNotice(e);
  }

  public static void notifyRoute(RouteMetric routeMetric) {
    notifier.routes.notify(routeMetric);
  }

  public static void notifyQueue(QueueMetric queueMetric) {
    notifier.queues.notify(queueMetric);
  }

  public static void notifyQuery(String method, String route, String query, Date startTime, Date endTime,
      String function, String file, int line) {

    notifier.queries.notify(method, route, query, startTime, endTime, function, file, line);
  }

  public static void notifyQuery(String method, String route, String query, Date startTime, Date endTime) {

    notifier.queries.notify(method, route, query, startTime, endTime);
  }
}
