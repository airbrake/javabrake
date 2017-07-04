package io.airbrake.javabrake;

public class NoticeStackRecord {
  static ClassUtil util = new ClassUtil();

  public final String function;
  public final String file;
  public final int line;

  public NoticeStackRecord(StackTraceElement el) {
    this.function = el.getMethodName();
    this.file = util.getFilepath(el.getClassName());
    this.line = el.getLineNumber();
  }

  public NoticeStackRecord(String function, String file, int line) {
    this.function = function;
    this.file = file;
    this.line = line;
  }
}
