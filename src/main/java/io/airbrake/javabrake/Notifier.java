package io.airbrake.javabrake;

import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

//** Airbrake notifier. */
public class Notifier {
  AsyncSender asyncSender;
  SyncSender syncSender;

  protected static List<Object> routeList = new ArrayList<>();
  protected static List<Object> routesBreakdownList = new ArrayList<>();
  protected static List<Object> queueList = new ArrayList<>();
  protected static List<Object> queryList = new ArrayList<>();

  public Routes routes;
  public Queries queries;
  public Queues queues;

  final List<NoticeHook> hooks = new ArrayList<>();
  final List<NoticeFilter> filters = new ArrayList<>();

  protected static Config config;

  protected static Notifier notifier;

  /**
   * @param config Configures the notifier
   */
  public Notifier(Config config) {
    Notifier.config = config;

    this.asyncSender = new OkAsyncSender(config);
    this.syncSender = new OkSyncSender(config);

    routes = new Routes();
    queries = new Queries();
    queues = new Queues();

    if (config.errorHost != null) {
      this.setErrorHost(config.errorHost);
    }

    if (config.apmHost != null) {
      this.setAPMHost(config.apmHost);
    }

    if (Airbrake.notifier == null) {
      Airbrake.notifier = this;
    }

    if (config.remoteConfig) {
      new RemoteSettings(
          config.projectId,
          "https://notifier-configs.airbrake.io",
          config,
          this.asyncSender,
          this.syncSender).poll();
    }
  }

  public Notifier setErrorHost(String host) {
    config.errorHost = host;
    this.asyncSender.setErrorHost(host);
    this.syncSender.setErrorHost(host);
    return this;
  }

  public Notifier setAPMHost(String host) {
    config.apmHost = host;
    this.asyncSender.setAPMHost(host);
    this.syncSender.setAPMHost(host);
    return this;
  }

  public Notifier setAsyncSender(AsyncSender sender) {
    this.asyncSender = sender;
    return this;
  }

  public Notifier setSyncSender(SyncSender sender) {
    this.syncSender = sender;
    return this;
  }

  public Notifier onReportedNotice(NoticeHook hook) {
    this.hooks.add(hook);
    return this;
  }

  /** Adds a filter that modifies or ignores a Notice. */
  public Notifier addFilter(NoticeFilter filter) {
    this.filters.add(filter);
    return this;
  }

  /** Asynchronously reports an exception to Airbrake. */
  public Future<Notice> report(Throwable e) {
    Notice notice = this.buildNotice(e);
    return this.send(notice);
  }

  /** Asynchronously sends a Notice to Airbrake. */
  public Future<Notice> send(Notice notice) {
    notice = this.filterNotice(notice);

    CompletableFuture<Notice> future = this.asyncSender.send(notice);

    final Notice finalNotice = notice;
    future.whenComplete(
        (value, exception) -> {
          this.applyHooks(finalNotice);
        });

    return future;
  }

  /** Sychronously reports an exception to Airbrake. */
  public Notice reportSync(Throwable e) {
    Notice notice = this.buildNotice(e);
    return this.sendSync(notice);
  }

  /** Synchronously sends a Notice to Airbrake. */
  public Notice sendSync(Notice notice) {
    if (!config.errorNotifications) {
      return notice;
    }

    notice = this.filterNotice(notice);
    notice = this.syncSender.send(notice);
    this.applyHooks(notice);
    return notice;
  }

  void applyHooks(Notice notice) {
    for (NoticeHook hook : this.hooks) {
      hook.hook(notice);
    }
  }

  Notice filterNotice(Notice notice) {
    for (NoticeFilter filter : this.filters) {
      notice = filter.filter(notice);
      if (notice == null) {
        return null;
      }
    }

    return notice;
  }

  /** Builds a Notice from an exception. */
  public Notice buildNotice(Throwable e) {
    return new Notice(e);
  }

  /** Get NoticeBuilder from an exception. */
  public NoticeBuilder noticeBuilder(Throwable e) {
    return new NoticeBuilder(e);
  }
}
