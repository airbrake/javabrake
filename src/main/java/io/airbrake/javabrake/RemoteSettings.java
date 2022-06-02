package io.airbrake.javabrake;

import java.util.Timer;

class RemoteSettings {
  // How frequently we should poll the config API (10 minutes).
  final private int DEFAULT_INTERVAL = 600000;

  final private int projectId;
  final private String host;
  final private Timer timer;
  final private AsyncSender asyncSender;
  final private SyncSender syncSender;

  final private Config config;

  public RemoteSettings(
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

    this.timer = new Timer();
  }

  public void poll() {
    PollTask pollTask = new PollTask(
      this.projectId,
      this.host,
      this.config,
      this.asyncSender,
      this.syncSender
    );
    this.timer.schedule(pollTask, 0, DEFAULT_INTERVAL);
  }
}
