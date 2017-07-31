package javabrake;

class AirbrakeStackRecord {
  String function;
  String file;
  int line;

  AirbrakeStackRecord(StackTraceElement el) {
    this.function = el.getMethodName();
    this.file = Util.getFilepath(el.getClassName());
    this.line = el.getLineNumber();
  }
}
