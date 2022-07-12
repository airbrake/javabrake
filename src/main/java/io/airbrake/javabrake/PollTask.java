package io.airbrake.javabrake;

import java.io.IOException;

import java.util.TimerTask;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.HttpUrl;
import com.google.gson.Gson;

import ch.qos.logback.classic.Logger;

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

  public String exceptionMessage = "";

  private static final Logger logger = (Logger) LoggerFactory.getLogger(PollTask.class);

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
    } catch (IOException e) {
     exceptionMessage = e.getMessage();
     logger.info(exceptionMessage);
     
     
     // Logger.getLogger("Airbrake Error").log(Level.INFO, exceptionMessage);
      return;
    }

    if(response == null || response.equals(""))
    {
    //  Logger.getLogger("Airbrake Error").log(Level.INFO, "Couldn't fetch remote config");
    }
    else
    if (!response.toLowerCase().contains("accessdenied")) {
      try {
        RemoteConfigJSON json_data = gson.fromJson(response, RemoteConfigJSON.class);
        this.data.merge(json_data);
      } catch (Exception e) {
      //  Logger.getLogger("Airbrake Error").log(Level.INFO, e.getMessage());
        return;
      }
    }
     else {
    //  Logger.getLogger("Airbrake Config Error").log(Level.INFO, "Please check your config details");
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
    this.config.errorHost = this.getErrorHost();
    this.asyncSender.setHost(this.config.errorHost);
    this.syncSender.setHost(this.config.errorHost);
  }


  public String getErrorHost() {
    String remoteErrorHost = this.data.errorHost();
    if (remoteErrorHost == null) {
      return Config.DEFAULT_ERROR_HOST;
    } else {
     return remoteErrorHost;
    }    
  }
  void processErrorNotifications(SettingsData data) {
    if (!this.origErrorNotifications) {
      return;
    }

    this.config.errorNotifications = data.errorNotifications();
  }
}
