package io.airbrake.javabrake;

import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import java.io.Reader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkAsyncSender extends OkSender implements AsyncSender {
  static final int queuedCallsLimit = 1000;
  static final IOException queuedCallsLimitException = new IOException("too many HTTP requests queued for execution");

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
        .newCall(this.buildErrorRequest(notice))
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

  @Override
  public CompletableFuture<ApmResponse> send(String body, String path) {
    CompletableFuture<ApmResponse> future = new CompletableFuture<>();

    okhttp
        .newCall(this.buildAPMRequest(body, path))
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {

                future.completeExceptionally(e);
              }

              @Override
              public void onResponse(Call call, Response resp) {
                ApmResponse apmResponse = null;
                if (resp != null) {
                  apmResponse = new ApmResponse();
                  apmResponse.message = resp.message();
                  apmResponse.code = resp.code();
                  resp.close();
                }
                
                future.complete(apmResponse);
              }
            });
    return future;
  }
}
