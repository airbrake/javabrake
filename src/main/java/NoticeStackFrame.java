package io.airbrake.javabrake;

import java.util.Map;

public class NoticeStackFrame {
  static ClassUtil util = new ClassUtil();
  static CodeHunk codeHunk = new CodeHunk();

  public final String function;
  public final String file;
  public final int line;
  public final Map<Integer, String> code;

  public NoticeStackFrame(StackTraceElement el) {
    this(el.getMethodName(), util.getFilepath(el.getClassName()), el.getLineNumber());
  }

  public NoticeStackFrame(String function, String file, int line) {
    this.function = function;
    this.file = file;
    this.line = line;
    this.code = codeHunk.get(file, line);
  }
}
