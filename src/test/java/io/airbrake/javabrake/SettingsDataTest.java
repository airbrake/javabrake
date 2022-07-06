package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

public class SettingsDataTest {
  Gson gson = new Gson();

  @Test
  public void testIntervalWhenNoPollSec() {
    RemoteConfigJSON rc = gson.fromJson(
      "{}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(600, data.interval());
  }

  @Test
  public void testIntervalWhenPollSecIsNull() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"poll_sec\":null}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(600, data.interval());
  }

  @Test
  public void testIntervalWhenPollSecIsLessThanZero() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"poll_sec\":-123}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(600, data.interval());
  }

  @Test
  public void testIntervalWhenPollSecIsGreaterThanZero() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"poll_sec\":999}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(999, data.interval());
  }

  @Test
  public void testConfigRouteWhenItIsSpecified() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"config_route\":\"123/cfg/321/cfg.json\"}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(
      "http://example.com/123/cfg/321/cfg.json",
      data.configRoute("http://example.com")
    );
  }

  @Test
  public void testConfigRouteWhenTheGivenHostEndsWithATrailingSlash() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"config_route\":\"cfg.json\"}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(
      "http://example.com/cfg.json",
      data.configRoute("http://example.com/")
    );
  }

  @Test
  public void testConfigRouteWhenThereIsNoConfigRoute() {
    RemoteConfigJSON rc = gson.fromJson(
      "{}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(
      "http://example.com/2020-06-18/config/123/config.json",
      data.configRoute("http://example.com")
    );
  }

  @Test
  public void testConfigRouteWhenItIsNull() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"config_route\":null}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(
      "http://example.com/2020-06-18/config/123/config.json",
      data.configRoute("http://example.com")
    );
  }

  @Test
  public void testConfigRouteWhenItIsEmpty() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"config_route\":\"\"}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals(
      "http://example.com/2020-06-18/config/123/config.json",
      data.configRoute("http://example.com")
    );
  }

  @Test
  public void testErrorNotificationsWhenItIsPresentAndEnabled() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"enabled\":true}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertTrue(data.errorNotifications());
  }

  @Test
  public void testErrorNotificationsWhenItIsPresentAndDisabled() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"enabled\":false}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertFalse(data.errorNotifications());
  }

  @Test
  public void testErrorNotificationsWhenItIsMissing() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertTrue(data.errorNotifications());
  }

  @Test
  public void testErrorNotificationsWhenSettingsAreMissing() {
    RemoteConfigJSON rc = gson.fromJson(
      "{}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertTrue(data.errorNotifications());
  }

  @Test
  public void testPerformanceStatsWhenItIsPresentAndEnabled() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"apm\",\"enabled\":true}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertTrue(data.performanceStats());
  }

  @Test
  public void testPerformanceStatsWhenItIsPresentAndDisabled() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"apm\",\"enabled\":false}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertFalse(data.performanceStats());
  }

  @Test
  public void testPerformanceStatsWhenItIsMissing() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertTrue(data.performanceStats());
  }

  @Test
  public void testPerformanceStatsWhenSettingsAreMissing() {
    RemoteConfigJSON rc = gson.fromJson(
      "{}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertTrue(data.performanceStats());
  }

  @Test
  public void testErrorHostWhenTheErrorsSettingIsPresent() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"enabled\":true,\"endpoint\":\"http://api.example.com\"}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals("http://api.example.com", data.errorHost());
  }

  @Test
  public void testErrorHostWhenTheErrorsSettingIsPresentWithoutEndpoint() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"enabled\":true}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertNull(data.errorHost());
  }

  @Test
  public void testErrorHostWhenTheErrorsSettingIsMissing() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertNull(data.errorHost());
  }

  @Test
  public void testApmHostWhenTheApmSettingIsPresent() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"apm\",\"enabled\":true,\"endpoint\":\"http://api.example.com\"}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertEquals("http://api.example.com", data.apmHost());
  }

  @Test
  public void testApmHostWhenTheErrorsSettingIsPresentWithoutEndpoint() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"enabled\":true}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertNull(data.apmHost());
  }

  @Test
  public void testApmHostWhenTheErrorsSettingIsMissing() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);
    assertNull(data.apmHost());
  }

  @Test
  public void testMergeEnabledSetting() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"enabled\":true}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);

    RemoteConfigJSON other = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"enabled\":false}]}",
      RemoteConfigJSON.class
    );

    data.merge(other);

    assertEquals(
      "{\"poll_sec\":0,\"settings\":[{\"name\":\"errors\",\"enabled\":false}]}",
      gson.toJson(data.data)
    );
  }

  @Test
  public void testMergeEndpointSetting() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"endpoint\":\"aaaaa\"}]}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);

    RemoteConfigJSON other = gson.fromJson(
      "{\"settings\":[{\"name\":\"errors\",\"endpoint\":\"bbbbb\"}]}",
      RemoteConfigJSON.class
    );

    data.merge(other);

    assertEquals(
      "{\"poll_sec\":0,\"settings\":[{\"name\":\"errors\",\"endpoint\":\"bbbbb\"}]}",
      gson.toJson(data.data)
    );
  }

  @Test
  public void testMergePollSec() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"poll_sec\":123}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);

    RemoteConfigJSON other = gson.fromJson(
      "{\"poll_sec\":321}",
      RemoteConfigJSON.class
    );

    data.merge(other);

    assertEquals(
      "{\"poll_sec\":321,\"settings\":[]}",
      gson.toJson(data.data)
    );
  }

  @Test
  public void testMergeConfigRoute() {
    RemoteConfigJSON rc = gson.fromJson(
      "{\"config_route\":\"aaaaa\"}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);

    RemoteConfigJSON other = gson.fromJson(
      "{\"config_route\":\"bbbbb\"}",
      RemoteConfigJSON.class
    );

    data.merge(other);

    assertEquals(
      "{\"config_route\":\"bbbbb\",\"poll_sec\":0,\"settings\":[]}",
      gson.toJson(data.data)
    );
  }

  @Test
  public void testMergeNewSetting() {
    RemoteConfigJSON rc = gson.fromJson(
      "{}",
      RemoteConfigJSON.class
    );
    SettingsData data = new SettingsData(123, rc);

    RemoteConfigJSON other = gson.fromJson(
      "{\"settings\":[{\"name\":\"apm\"}]}",
      RemoteConfigJSON.class
    );

    data.merge(other);

    assertEquals(
      "{\"poll_sec\":0,\"settings\":[{\"name\":\"apm\"}]}",
      gson.toJson(data.data)
    );
  }
}
