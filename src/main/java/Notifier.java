package io.airbrake.javabrake;

import java.util.concurrent.Future;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/** Airbrake notifier. */
public class Notifier {
  AsyncSender asyncSender;
  SyncSender syncSender;

  final List<NoticeHook> hooks = new ArrayList<>();
  final List<NoticeFilter> filters = new ArrayList<>();

  final private Config config;

  /**
   * @param config Configures the notifier
   */
  public Notifier(Config config) {
    this.config = config;

    this.asyncSender = new OkAsyncSender(config);
    this.syncSender = new OkSyncSender(config);

    if (config.errorHost != null) {
      this.setHost(config.errorHost);
    }

    if (Airbrake.notifier == null) {
      Airbrake.notifier = this;
    }
  }

  public Notifier setHost(String host) {
    this.asyncSender.setHost(host);
    this.syncSender.setHost(host);
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
}
