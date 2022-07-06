package io.airbrake.javabrake;

@FunctionalInterface
public interface NoticeHook {
  void hook(Notice notice);
}
