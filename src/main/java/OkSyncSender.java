package io.airbrake.javabrake;

import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

class OkSyncSender extends OkSender implements SyncSender {
  public OkSyncSender(int projectId, String projectKey) {
    super(projectId, projectKey);
  }

  @Override
  public Notice send(Notice notice) {
    if (notice == null) {
      return null;
    }

    long utime = System.currentTimeMillis() / 1000L;
    if (utime < this.rateLimitReset.get()) {
      notice.exception = OkSender.projectRateLimitedException;
      return notice;
    }

    Call call = OkSyncSender.okhttp.newCall(this.buildRequest(notice));

    Response resp;
    try {
      resp = call.execute();
    } catch (IOException e) {
      notice.exception = e;
      return notice;
    }

    this.parseResponse(resp, notice);
    return notice;
  }
}
