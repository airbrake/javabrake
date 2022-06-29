package io.airbrake.javabrake;

import java.io.IOException;

import java.util.TimerTask;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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

  final private static HashMap<String, String> NOTIFIER_INFO;
  static {
    NOTIFIER_INFO = new HashMap<String, String>();
    NOTIFIER_INFO.put("notifier_name", Notice.notifierInfo.get("name"));
    NOTIFIER_INFO.put("notifier_version", Notice.notifierInfo.get("version"));
    NOTIFIER_INFO.put("os", System.getProperty("os.name") + "/" + System.getProperty("os.version"));
    NOTIFIER_INFO.put("language", "Java/" + System.getProperty("java.version"));
  }

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
    } catch(IOException e) {
      e.printStackTrace();
      return;
    }

    try {
      RemoteConfigJSON json_data = gson.fromJson(response, RemoteConfigJSON.class);
      this.data.merge(json_data);
    } catch(JsonSyntaxException e) {
      e.printStackTrace();
      return;
    }

    this.setErrorHost(this.data);
    this.processErrorNotifications(this.data);
  }

  String request() throws IOException {
    HttpUrl.Builder httpBuilder = HttpUrl.parse(this.data.configRoute(this.host))
      .newBuilder();
    for (HashMap.Entry<String, String> param : NOTIFIER_INFO.entrySet()) {
      httpBuilder.addQueryParameter(param.getKey(), param.getValue());
    }

    Request request = new Request.Builder()
      .url(httpBuilder.build())
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
