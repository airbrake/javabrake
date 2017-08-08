package io.airbrake.javabrake;

import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

class OkSyncReporter extends OkReporter implements SyncReporter {
  OkSyncReporter(int projectId, String projectKey) {
    super(projectId, projectKey);
  }

  public Notice report(Notice notice) {
    if (notice == null) {
      return null;
    }

    Call call = OkSyncReporter.okhttp.newCall(this.buildRequest(notice));

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
