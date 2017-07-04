package javabrake;

import java.util.List;
import java.util.ArrayList;

class AirbrakeError {
  public String type;
  public String message;
  public List<AirbrakeStackRecord> backtrace = new ArrayList<>();

  AirbrakeError(Throwable e) {
    this.type = e.getClass().getCanonicalName();
    this.message = e.getMessage();
    for (StackTraceElement el : e.getStackTrace()) {
      this.backtrace.add(new AirbrakeStackRecord(el));
    }
  }
}
