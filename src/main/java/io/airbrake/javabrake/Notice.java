package io.airbrake.javabrake;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import okhttp3.Response;

public class Notice {
  public static final HashMap<String, String> notifierInfo;

  static {
    notifierInfo = new HashMap<>();
    notifierInfo.put("name", "javabrake");
    notifierInfo.put("version", "0.3.0");
    notifierInfo.put("url", "https://github.com/airbrake/javabrake");
  }

  public String id;
  public String url;
  /** Marked transient so gson will ignore this field during serialization **/
  /**
   * https://github.com/google/gson/blob/master/UserGuide.md#java-modifier-exclusion
   **/
  /** Exception occurred reporting this Notice. */
  public transient Throwable exception;
  protected transient Response response;

  public List<NoticeError> errors;
  @Nullable
  public Map<String, Object> context;
  @Nullable
  public Map<String, Object> params;
  @Nullable
  public Map<String, Object> session;
  @Nullable
  public Map<String, Object> environment;

  public Notice() {
    this.setContext("notifier", notifierInfo);
    String lang = "Java/" + System.getProperty("java.version");
    this.setContext("language", lang);
    String os = System.getProperty("os.name") + "/" + System.getProperty("os.version");
    this.setContext("os", os);
    this.setContext("architecture", System.getProperty("os.arch"));

    try {
      String hostname = InetAddress.getLocalHost().getHostName();
      this.setContext("hostname", hostname);
    } catch (UnknownHostException ex) {
    }
  }

  public Notice(NoticeBuilder noticeBuilder) {
    this();
    if (noticeBuilder.exception != null) {
      this.errors = new ArrayList<>();
      while (noticeBuilder.exception != null) {
        this.errors.add(new NoticeError(noticeBuilder.exception));
        noticeBuilder.exception = noticeBuilder.exception.getCause();
      }
    } else
      this.errors = noticeBuilder.errors;

    this.context = noticeBuilder.context;
    this.params = noticeBuilder.params;
    this.session = noticeBuilder.session;
    this.environment = noticeBuilder.environment;
  }

  public Notice(List<NoticeError> errors) {
    this();
    this.errors = errors;
  }

  public Notice(Throwable e) {
    this();
    this.errors = new ArrayList<>();
    while (e != null) {
      this.errors.add(new NoticeError(e));
      e = e.getCause();
    }
  }

  public String toString() {
    if (this.errors.size() == 0) {
      return "Notice<no errors>";
    }
    NoticeError err = this.errors.get(0);
    return String.format("Notice<type=`%s` message=`%s`>", err.type, err.message);
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
