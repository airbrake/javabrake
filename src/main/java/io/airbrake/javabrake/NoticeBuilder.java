package io.airbrake.javabrake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeBuilder {
  Map<String, Object> context;
  Map<String, Object> params;
  Map<String, Object> session;
  Map<String, Object> environment;
  Throwable exception;
  List<NoticeError> errors;

  public NoticeBuilder(Throwable exception) {
    this.exception = exception;
  }

  public NoticeBuilder(List<NoticeError> errors) {
    this.errors = errors;
  }

  public NoticeBuilder environment(Object value) {
    if (this.environment == null) {
      this.environment = new HashMap<>();
    }
    this.environment.put("environment", value);
    return this;
  }

  public NoticeBuilder component(Object value) {
    if (this.context == null) {
      this.context = new HashMap<>();
    }
    this.context.put("component", value);
    return this;
  }

  public NoticeBuilder severity(Object value) {
    if (this.context == null) {
      this.context = new HashMap<>();
    }
    this.context.put("severity", value);
    return this;
  }

  public NoticeBuilder session(Object value) {
    if (this.session == null) {
      this.session = new HashMap<>();
    }
    this.session.put("session", value);
    return this;
  }

  public NoticeBuilder context(String key, Object value) {
    if (this.context == null) {
      this.context = new HashMap<>();
    }
    this.context.put(key, value);
    return this;
  }

  public NoticeBuilder param(String key, Object value) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.put(key, value);
    return this;
  }

  public Notice build() {
    return new Notice(this);
  }
}
