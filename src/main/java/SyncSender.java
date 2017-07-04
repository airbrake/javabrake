package io.airbrake.javabrake;

public interface SyncSender {
  Notice send(Notice notice);
}
