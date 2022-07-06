package io.airbrake.javabrake;

public class NoticeError {
  public final String type;
  public final String message;
  public final NoticeStackFrame[] backtrace;

  public NoticeError(Throwable e) {
    this(e.getClass().getCanonicalName(), e.getMessage(), e.getStackTrace());
  }

  public NoticeError(String type, String message, StackTraceElement[] stackTrace) {
    this.type = type;
    this.message = message;
    this.backtrace = new NoticeStackFrame[stackTrace.length];
    for (int i = 0; i < stackTrace.length; i++) {
      this.backtrace[i] = new NoticeStackFrame(stackTrace[i]);
    }
  }
}
