package io.airbrake.javabrake;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.RequestBody;
import java.io.IOException;

class OkSender {
  static final int maxNoticeSize = 64000;
  static final MediaType JSONType = MediaType.parse("application/json");

  static final Gson gson = new GsonBuilder().create();
  static final OkHttpClient okhttp = new OkHttpClient();

  final int projectId;
  final String projectKey;

  public OkSender(int projectId, String projectKey) {
    this.projectId = projectId;
    this.projectKey = projectKey;
  }

  Request buildRequest(Notice notice) {
    String data = this.noticeJson(notice);
    RequestBody body = RequestBody.create(JSONType, data);
    return new Request.Builder()
        .header("X-Airbrake-Token", this.projectKey)
        .url(this.getUrl())
        .post(body)
        .build();
  }

  String noticeJson(Notice notice) {
    String data = "";
    Truncator truncator = null;
    for (int level = 0; level < 8; level++) {
      data = OkSender.gson.toJson(notice);
      if (data.length() < OkSender.maxNoticeSize) {
        break;
      }

      if (truncator == null) {
        truncator = new Truncator(level);
      }
      notice.context = truncator.truncateMap(notice.context, 0);
      notice.params = truncator.truncateMap(notice.params, 0);
      notice.session = truncator.truncateMap(notice.session, 0);
      notice.environment = truncator.truncateMap(notice.environment, 0);
    }
    return data;
  }

  String getUrl() {
    return String.format("https://api.airbrake.io/api/v3/projects/%d/notices", this.projectId);
  }

  void parseResponse(Response resp, Notice notice) {
    if (resp.isSuccessful()) {
      NoticeIdURL data = OkSender.gson.fromJson(resp.body().charStream(), NoticeIdURL.class);
      notice.id = data.id;
      notice.url = data.url;
      return;
    }

    if (resp.code() >= 400 && resp.code() < 500) {
      NoticeCode data = OkSender.gson.fromJson(resp.body().charStream(), NoticeCode.class);
      notice.exception = new IOException(data.message);
    } else {
      notice.exception = new IOException("unexpected response " + resp);
    }
  }
}
