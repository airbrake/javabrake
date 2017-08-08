package io.airbrake.javabrake;

import java.util.concurrent.Future;

public class Airbrake {
  static Notifier notifier;

  public static void setNotifier(Notifier notifier) {
    Airbrake.notifier = notifier;
  }

  public static Future<Notice> report(Throwable e) {
    return Airbrake.notifier.report(e);
  }

  public static Notice reportSync(Throwable e) {
    return Airbrake.notifier.reportSync(e);
  }
}
