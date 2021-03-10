package io.airbrake.javabrake;

import java.util.Timer;

public class RemoteSettings {
  final private int projectId;
  final private String host;
  final private Timer timer;
  final private Config config;

  public RemoteSettings(int projectId, String host, Config config) {
    this.projectId = projectId;
    this.host = host;
    this.config = config;
    this.timer = new Timer();
  }

  public void poll() {
    PollTask pollTask = new PollTask(this.projectId, this.host, this.config);
    this.timer.schedule(pollTask, 0, 1*5000);
  }
}
