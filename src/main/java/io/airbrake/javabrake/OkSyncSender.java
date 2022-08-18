package io.airbrake.javabrake;

import java.io.IOException;
import okhttp3.Call;
import okhttp3.Response;

public class OkSyncSender extends OkSender implements SyncSender {
  public OkSyncSender(Config config) {
    super(config);
  }

  @Override
  public Notice send(Notice notice) {
    if (notice == null) {
      return null;
    }

    long utime = System.currentTimeMillis() / 1000L;
    if (utime < this.rateLimitReset.get()) {
      notice.exception = ipRateLimitedException;
      return notice;
    }

    Call call = okhttp.newCall(this.buildErrorRequest(notice));
    try (Response resp = call.execute()) {
      this.parseResponse(resp, notice);
    } catch (IOException e) {
      notice.exception = e;
      return notice;
    }

    return notice;
  }
}
