package io.airbrake.javabrake;

public class Config {
  public static final String DEFAULT_ERROR_HOST = "https://api.airbrake.io";

  public int projectId;
  public String projectKey;
  public String errorHost = DEFAULT_ERROR_HOST;
  public Boolean errorNotifications = true;
  public Boolean remoteConfig = true;
}
