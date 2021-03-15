package io.airbrake.javabrake;

public class Config {
  public int projectId;
  public String projectKey;
  public String errorHost = "https://api.airbrake.io";
  public Boolean errorNotifications = true;
}
