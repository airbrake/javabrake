package io.airbrake.javabrake;

public interface SyncSender {
  void setHost(String host);

  Notice send(Notice notice);
}
