package io.airbrake.javabrake;

import java.util.concurrent.CompletableFuture;

public interface AsyncSender {
  CompletableFuture<Notice> send(Notice notice);
}
