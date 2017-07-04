package io.airbrake.javabrake;

import java.util.List;
import java.util.ArrayList;

public class NoticeError {
  public final String type;
  public final String message;
  public final List<NoticeStackRecord> backtrace;

  public NoticeError(Throwable e) {
    this.type = e.getClass().getCanonicalName();
    this.message = e.getMessage();

    this.backtrace = new ArrayList<>();
    for (StackTraceElement el : e.getStackTrace()) {
      this.backtrace.add(new NoticeStackRecord(el));
    }
  }

  public NoticeError(String type, String message, List<NoticeStackRecord> backtrace) {
    this.type = type;
    this.message = message;
    this.backtrace = backtrace;
  }
}
