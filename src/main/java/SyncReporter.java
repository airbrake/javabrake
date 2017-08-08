package io.airbrake.javabrake;

public interface SyncReporter {
  Notice report(Notice notice);
}
