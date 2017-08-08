package io.airbrake.javabrake;

import javax.annotation.Nullable;

@FunctionalInterface
public interface NoticeFilter {
  @Nullable
  Notice filter(Notice notice);
}
