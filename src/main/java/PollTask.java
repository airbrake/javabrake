package io.airbrake.javabrake;

import java.io.IOException;

import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

class PollTask extends TimerTask {
  final private int projectId;
  final private String host;
  final private Config config;
  final private AsyncSender asyncSender;
  final private SyncSender syncSender;

  final private SettingsData data;

  final private Boolean origErrorNotifications;

  final private OkHttpClient client = new OkHttpClient();
  final private Gson gson = new Gson();

  public PollTask(
    int projectId,
    String host,
    Config config,
    AsyncSender asyncSender,
    SyncSender syncSender
  ) {
    this.projectId = projectId;
    this.host = host;
    this.config = config;
    this.asyncSender = asyncSender;
    this.syncSender = syncSender;

    this.data = new SettingsData(this.projectId, new RemoteConfigJSON());

    this.origErrorNotifications = config.errorNotifications;
  }

  public void run() {
    String response = null;
    try {
      response = this.request();
    } catch(IOException ex) {
    }

    RemoteConfigJSON json_data = gson.fromJson(response, RemoteConfigJSON.class);
    this.data.merge(json_data);

    this.setErrorHost(this.data);
    this.processErrorNotifications(this.data);
  }

  String request() throws IOException {
    Request request = new Request.Builder()
      .url(this.data.configRoute(this.host))
      .build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }

  void setErrorHost(SettingsData data) {
    String remoteErrorHost = this.data.errorHost();
    if (remoteErrorHost == null) {
      this.config.errorHost = Config.DEFAULT_ERROR_HOST;
    } else {
      this.config.errorHost = remoteErrorHost;
    }

    this.asyncSender.setHost(this.config.errorHost);
    this.syncSender.setHost(this.config.errorHost);
  }

  void processErrorNotifications(SettingsData data) {
    if (!this.origErrorNotifications) {
      return;
    }

    this.config.errorNotifications = data.errorNotifications();
  }
}
