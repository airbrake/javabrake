package io.airbrake.javabrake;

public interface SyncSender {
  void setErrorHost(String host);
  void setAPMHost(String host);
  Notice send(Notice notice);
}
