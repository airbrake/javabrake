package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.github.tomakehurst.wiremock.WireMockServer;

public class RouteTest {
  static WireMockServer wireMockServer = null;
  static Notifier notifier;
  Throwable exc = new IOException("hello from Java");
  static Config config = null;

  @BeforeAll
  public static void init() {
    wireMockServer = new WireMockServer(); // No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();
    config = new Config();
    config.remoteConfig = false;
    notifier = new Notifier(config);
  }

  public RouteStats getRouteStats() {
    RouteMetric metric = new RouteMetric("GET", "/test", 200, "application/json");

    Metrics.FLUSH_PERIOD = 5;
    metric.endTime = new Date();

    String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(metric.startTime);
    RouteStats stat = new RouteStats(
        metric.method,
        metric.route,
        metric.statusCode,
        date);

    long ms = metric.endTime.getTime() - metric.startTime.getTime();
    stat.add(ms);
    return stat;
  }

  @Test
  public void testRoutesNotifyTryCatch() {

    config.apmNotifications = false;
    RouteMetric metric = new RouteMetric("GET", "/test", 200, "application/json");
    try {
      RouteStats.notify(metric,config);
    } catch (NullPointerException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testRoutesNotify() {

    config.apmNotifications = true;
    config.projectId = 0;
    notifier.setAPMHost("http://localhost:8080");
    RouteMetric metric = new RouteMetric("GET", "/test", 200, "application/json");

    RouteMetric.FLUSH_PERIOD = 50;
    metric.endTime = new Date();

    stubFor(post(urlEqualTo("/api/v5/projects/0/routes-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      RouteStats.notify(metric,config);
      Thread.sleep(5000);
    } catch (Exception e) {
      assertTrue(false);
    }

    assertEquals(RouteStats.exception, null);
  }

  @Test
  public void testRouteNotifyException() {

    config.apmNotifications = true;
    config.projectId = 1;
    notifier.setAPMHost("http://localhost:8080");

    RouteMetric metric = new RouteMetric();

    stubFor(post(urlEqualTo("/api/v5/projects/1/routes-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      RouteStats.notify(metric,config);
      Thread.sleep(5000);

    } catch (Exception e) {
      assertTrue(false);
    }
    assertEquals(RouteStats.exception.toString(), "java.lang.NullPointerException");
  }

  @AfterAll
  public static void closeWireMockServer() {
    wireMockServer.stop();
  }
}
