package io.airbrake.javabrake;

import java.util.concurrent.CompletableFuture;

public interface AsyncSender {
  void setHost(String host);

  CompletableFuture<Notice> send(Notice notice);
}
