package io.airbrake.javabrake;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkSender {
  static final int maxNoticeSize = 64000;
  static final MediaType JSONType = MediaType.parse("application/json");

  static final Gson gson = new GsonBuilder().create();
  static OkHttpClient okhttp = new OkHttpClient.Builder()
      .connectTimeout(3000, TimeUnit.MILLISECONDS)
      .readTimeout(3000, TimeUnit.MILLISECONDS)
      .writeTimeout(3000, TimeUnit.MILLISECONDS)
      .build();

  static final IOException ipUnauthorizedException = new IOException("unauthorized: project id or key are wrong");
  static final IOException ipRateLimitedException = new IOException("IP is rate limited");

  final Config config;
  final int projectId;
  final String projectKey;
  String apmUrl;
  String errorUrl;

  final AtomicLong rateLimitReset = new AtomicLong(0);

  public OkSender(Config config) {
    this.config = config;
    this.projectId = config.projectId;
    this.projectKey = config.projectKey;
    this.errorUrl = this.buildErrorUrl(config.errorHost);
    this.apmUrl = this.buildAPMUrl(config.errorHost);
  }

  public static void setOkHttpClient(OkHttpClient okhttp) {
    OkSender.okhttp = okhttp;
  }

  public void setErrorHost(String host) {
    this.errorUrl = this.buildErrorUrl(host);
  }

  public void setAPMHost(String host) {
    this.apmUrl = this.buildAPMUrl(host);
  }

  Request buildErrorRequest(Notice notice) {
    this.errorUrl = this.buildErrorUrl(config.errorHost);
    String data = this.noticeJson(notice);
    RequestBody body = RequestBody.create(data, JSONType);
    return new Request.Builder()
        .header("Authorization", "Bearer " + this.projectKey)
        .url(this.errorUrl)
        .post(body)
        .build();
  }

  Request buildAPMRequest(String json, String method) {
    this.apmUrl = this.buildAPMUrl(method);
    RequestBody body = RequestBody.create(json, JSONType);
    return new Request.Builder()
        .header("Authorization", "Bearer " + this.projectKey)
        .url(this.apmUrl)
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

  String buildErrorUrl(String host) {
    return String.format("%s/api/v3/projects/%d/notices", host, this.projectId);
  }

  String buildAPMUrl(String path) {
    return String.format("%s/api/v5/projects/%d/%s", config.apmHost, this.projectId, path);
  }

  void parseResponse(Response resp, Notice notice) {
    if (resp.isSuccessful()) {
      try {
        NoticeIdURL data = this.parseJson(resp, NoticeIdURL.class);
        notice.id = data.id;
        notice.url = data.url;
      } catch (JsonParseException e) {
        notice.exception = e;
      }
      return;
    }

    if (resp.code() == 401 || resp.code() == 403) {
      notice.exception = ipUnauthorizedException;
      return;
    }
    
    if (resp.code() >= 400 && resp.code() < 500) {
      try {
        NoticeCode data = this.parseJson(resp, NoticeCode.class);
        notice.exception = new IOException(data.message);

        if (resp.code() == 429 && data.message.contains("Too many requests from your IP address")) {
          String header = resp.header("X-RateLimit-Delay");
          if (header == null) {
            return;
          }

          long n = 0;
          try {
            n = Long.parseLong(header);
          } catch (NumberFormatException e) {
          }
          if (n > 0) {
            this.rateLimitReset.set(System.currentTimeMillis() / 1000L + n);
          }
          return;
        }
      } catch (JsonParseException e) {
        notice.exception = e;
      }
      return;
    }

    notice.exception = new IOException("unexpected response " + resp);
  }

  <T> T parseJson(Response resp, Class<T> classOfT) {
    try (ResponseBody body = resp.body()) {
      Reader stream = body.charStream();
      return OkSender.gson.fromJson(stream, classOfT);
    }
  }
}
