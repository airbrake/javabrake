package javabrake;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;
import okhttp3.Response;

class Notice {
  public String id;
  public String url;
  public Throwable exception;

  public List<AirbrakeError> errors;
  @Nullable public Map<String, Object> context;
  @Nullable public Map<String, Object> params;
  @Nullable public Map<String, Object> session;
  @Nullable public Map<String, Object> environment;

  Notice(Throwable e) {
    this.errors = new ArrayList<>();
    this.errors.add(new AirbrakeError(e));
  }

  public Notice setContext(String key, Object value) {
    if (this.context == null) {
      this.context = new HashMap<>();
    }
    this.context.put(key, value);
    return this;
  }

  public Notice setParam(String key, Object value) {
    if (this.params == null) {
      this.params = new HashMap<>();
    }
    this.params.put(key, value);
    return this;
  }

  public Notice setSession(String key, Object value) {
    if (this.session == null) {
      this.session = new HashMap<>();
    }
    this.session.put(key, value);
    return this;
  }

  public Notice setEnvironment(String key, Object value) {
    if (this.environment == null) {
      this.environment = new HashMap<>();
    }
    this.environment.put(key, value);
    return this;
  }
}
