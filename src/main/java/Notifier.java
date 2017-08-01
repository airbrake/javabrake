package javabrake;

import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import javax.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Notifier {
  static final MediaType JSONType = MediaType.parse("application/json");

  final int projectId;
  final String projectKey;

  final Gson gson = new GsonBuilder().create();
  final OkHttpClient client = new OkHttpClient();

  final List<NoticeFilter> filters = new ArrayList<>();

  Notifier(int projectId, String projectKey) {
    this.projectId = projectId;
    this.projectKey = projectKey;
  }

  public Notifier setRootDirectory(String dir) {
    if (dir != "") {
      char ch = dir.charAt(dir.length() - 1);
      if (ch != '/' && ch != '\\') {
        dir += '/';
      }
    }

    Util.rootDir = dir;
    return this;
  }

  public Notifier addFilter(NoticeFilter filter) {
    this.filters.add(filter);
    return this;
  }

  public Future<Notice> report(Throwable e) {
    Notice notice = this.buildNotice(e);
    return this.sendNotice(notice);
  }

  public Notice reportSync(Throwable e) {
    Notice notice = this.buildNotice(e);
    return this.sendNoticeSync(notice);
  }

  public Notice buildNotice(Throwable e) {
    Notice notice = new Notice(e);
    if (Util.rootDir != null) {
      notice.setContext("rootDirectory", Util.rootDir);
    }

    for (NoticeFilter filter : this.filters) {
      notice = filter.filter(notice);
      if (notice == null) {
        return null;
      }
    }

    return notice;
  }

  public Future<Notice> sendNotice(Notice notice) {
    CompletableFuture<Notice> future = new CompletableFuture<>();

    if (notice == null) {
      future.completeExceptionally(new IOException("javabrake: notice is ignored"));
      return future;
    }

    Notifier notifier = this;
    this.client
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

  public Notice sendNoticeSync(Notice notice) {
    if (notice == null) {
      return null;
    }

    Call call = client.newCall(this.buildRequest(notice));

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

  Request buildRequest(Notice notice) {
    String data = this.gson.toJson(notice);
    RequestBody body = RequestBody.create(JSONType, data);
    return new Request.Builder()
        .header("X-Airbrake-Token", this.projectKey)
        .url(this.getUrl())
        .post(body)
        .build();
  }

  String getUrl() {
    return String.format("https://api.airbrake.io/api/v3/projects/%d/notices", this.projectId);
  }

  void parseResponse(Response resp, Notice notice) {
    if (!resp.isSuccessful()) {
      notice.exception = new IOException("javabrake: unexpected response " + resp);
      return;
    }

    NoticeIdURL data = this.gson.fromJson(resp.body().charStream(), NoticeIdURL.class);
    notice.id = data.id;
    notice.url = data.url;
  }
}
