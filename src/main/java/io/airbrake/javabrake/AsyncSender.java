package io.airbrake.javabrake;

import java.util.concurrent.CompletableFuture;

public interface AsyncSender {
  void setErrorHost(String host);
  void setAPMHost(String host);

  CompletableFuture<Notice> send(Notice notice);
  CompletableFuture<ApmResponse> send(String body, String path);
}
