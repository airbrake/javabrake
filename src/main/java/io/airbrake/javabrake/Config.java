package io.airbrake.javabrake;

public class Config {
  public static final String DEFAULT_ERROR_HOST = "https://api.airbrake.io";
  public static final String DEFAULT_APM_HOST = "https://api.airbrake.io";
  public int projectId;
  public String projectKey;
  public String errorHost = DEFAULT_ERROR_HOST;
  public String apmHost = DEFAULT_APM_HOST;
  public Boolean errorNotifications = true;
  public Boolean remoteConfig = true;
  public String environment = "production"; 
  public boolean performanceStats = true;
  public boolean queryStats = true;
  public boolean queueStats = true;
  public boolean backlogEnabled = true;
  public int maxBacklogSize = 100;
}
