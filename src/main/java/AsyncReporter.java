package io.airbrake.javabrake;

import java.util.concurrent.Future;

public interface AsyncReporter {
  Future<Notice> report(Notice notice);
}
