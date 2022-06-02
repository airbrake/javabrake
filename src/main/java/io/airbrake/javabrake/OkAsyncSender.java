package io.airbrake.javabrake;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OkAsyncSender extends OkSender implements AsyncSender {
  static final int queuedCallsLimit = 1000;
  static final IOException queuedCallsLimitException =
      new IOException("too many HTTP requests queued for execution");

  public OkAsyncSender(Config config) {
    super(config);
  }

  @Override
  public CompletableFuture<Notice> send(Notice notice) {
    CompletableFuture<Notice> future = new CompletableFuture<>();

    if (!config.errorNotifications) {
      future.completeExceptionally(new IOException("errorNotifications is disabled"));
      return future;
    }

    if (notice == null) {
      future.completeExceptionally(new IOException("notice is null"));
      return future;
    }

    long utime = System.currentTimeMillis() / 1000L;
    if (utime < this.rateLimitReset.get()) {
      notice.exception = ipRateLimitedException;
      future.completeExceptionally(notice.exception);
      return future;
    }

    if (okhttp.dispatcher().queuedCallsCount() > queuedCallsLimit) {
      notice.exception = queuedCallsLimitException;
      future.completeExceptionally(notice.exception);
      return future;
    }

    OkAsyncSender sender = this;
    okhttp
        .newCall(this.buildRequest(notice))
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                notice.exception = e;
                future.completeExceptionally(notice.exception);
              }

              @Override
              public void onResponse(Call call, Response resp) {
                sender.parseResponse(resp, notice);
                resp.close();
                if (notice.exception != null) {
                  future.completeExceptionally(notice.exception);
                } else {
                  future.complete(notice);
                }
              }
            });
    return future;
  }
}
