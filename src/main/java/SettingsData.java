package io.airbrake.javabrake;

import java.util.ArrayList;

import com.google.gson.Gson;

class SettingsData {
  // API version to poll.
  static final String API_VER = "2020-06-18";

  // What path to poll.
  static final String CONFIG_ROUTE_PATTERN = "%s/%s/config/%d/config.json";

  // How frequently we should poll the config API.
  static final int DEFAULT_INTERVAL = 600;

  final private int projectId;
  RemoteConfigJSON data;

  public SettingsData(int projectId, RemoteConfigJSON data) {
    this.projectId = projectId;
    this.data = data;
  }

  public void merge(RemoteConfigJSON other) {
    this.data.merge(other);
  }

  public int interval() {
    if (this.data.poll_sec > 0) {
      return this.data.poll_sec;
    }

    return DEFAULT_INTERVAL;
  }

  public String configRoute(String remoteConfigHost) {
    if (remoteConfigHost.endsWith("/")) {
      remoteConfigHost = remoteConfigHost.substring(0, remoteConfigHost.length() - 1);
    }

    String configRoute = this.data.config_route;
    if (!(configRoute == null || configRoute.isEmpty())) {
      return remoteConfigHost + "/" + configRoute;
    }

    return String.format(CONFIG_ROUTE_PATTERN,
                         remoteConfigHost, API_VER, this.projectId);
  }

  public Boolean errorNotifications() {
    RemoteSettingJSON s = this.findSetting("errors");
    if (s == null) {
      return true;
    }

    return s.enabled;
  }

  public Boolean performanceStats() {
    RemoteSettingJSON s = this.findSetting("apm");
    if (s == null) {
      return true;
    }

    return s.enabled;
  }

  public String errorHost() {
    RemoteSettingJSON s = this.findSetting("errors");
    if (s == null) {
      return null;
    }

    return s.endpoint;
  }

  public String apmHost() {
    RemoteSettingJSON s = this.findSetting("apm");
    if (s == null) {
      return null;
    }

    return s.endpoint;
  }

  private RemoteSettingJSON findSetting(String name) {
    ArrayList<RemoteSettingJSON> settings = this.data.settings;

    if (settings.size() == 0) {
      return null;
    }

    for (RemoteSettingJSON s : settings) {
      if (s.name.equals(name)) {
        return s;
      }
    }
    return null;
  }
}
