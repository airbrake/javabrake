package io.airbrake.javabrake;

import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

class OkAsyncReporter extends OkReporter implements AsyncReporter {
  OkAsyncReporter(int projectId, String projectKey) {
    super(projectId, projectKey);
  }

  public Future<Notice> report(Notice notice) {
    CompletableFuture<Notice> future = new CompletableFuture<>();

    if (notice == null) {
      future.completeExceptionally(new IOException("javabrake: notice is ignored"));
      return future;
    }

    OkAsyncReporter notifier = this;
    OkAsyncReporter.okhttp
        .newCall(this.buildRequest(notice))
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
              }

              @Override
              public void onResponse(Call call, Response resp) {
                notifier.parseResponse(resp, notice);
                future.complete(notice);
              }
            });
    return future;
  }
}
