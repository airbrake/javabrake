package io.airbrake.javabrake;

import java.util.concurrent.Future;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import javax.annotation.Nullable;

public class Notifier {
  AsyncReporter asyncReporter;
  SyncReporter syncReporter;

  final List<NoticeFilter> filters = new ArrayList<>();

  Notifier(int projectId, String projectKey) {
    this.asyncReporter = new OkAsyncReporter(projectId, projectKey);
    this.syncReporter = new OkSyncReporter(projectId, projectKey);
  }

  public Notifier setAsyncReporter(AsyncReporter reporter) {
    this.asyncReporter = reporter;
    return this;
  }

  public Notifier setSyncReporter(SyncReporter reporter) {
    this.syncReporter = reporter;
    return this;
  }

  public Notifier setRootDirectory(String dir) {
    if (dir != "") {
      char ch = dir.charAt(dir.length() - 1);
      if (ch != '/' && ch != '\\') {
        dir += '/';
      }
    }

    Util.rootDir = dir;
    return this;
  }

  public Notifier addFilter(NoticeFilter filter) {
    this.filters.add(filter);
    return this;
  }

  public Future<Notice> report(Throwable e) {
    Notice notice = this.buildNotice(e);
    return this.asyncReporter.report(notice);
  }

  public Notice reportSync(Throwable e) {
    Notice notice = this.buildNotice(e);
    return this.syncReporter.report(notice);
  }

  public Notice buildNotice(Throwable e) {
    Notice notice = new Notice(e);
    if (Util.rootDir != null) {
      notice.setContext("rootDirectory", Util.rootDir);
    }

    for (NoticeFilter filter : this.filters) {
      notice = filter.filter(notice);
      if (notice == null) {
        return null;
      }
    }

    return notice;
  }
}
