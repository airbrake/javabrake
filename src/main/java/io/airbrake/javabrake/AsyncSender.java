package io.airbrake.javabrake;

import java.util.concurrent.CompletableFuture;

import okhttp3.Response;

public interface AsyncSender {
  void setErrorHost(String host);
  void setAPMHost(String host);

  CompletableFuture<Notice> send(Notice notice);
  CompletableFuture<Response> send(Routes object);
  CompletableFuture<Response> send(Queries object);
}
