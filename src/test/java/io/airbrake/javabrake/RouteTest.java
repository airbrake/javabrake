package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.github.tomakehurst.wiremock.WireMockServer;

@TestMethodOrder(OrderAnnotation.class)
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
  @Order(1)
  public void testRoutesNotifyTryCatch() {

    config.performanceStats = false;
    RouteMetric metric = new RouteMetric("GET", "/test", 200, "application/json");
    try {
      notifier.route.notify(metric);
    } 
    // catch (NullPointerException e) {
    //   //assertTrue(true);
    // }
    finally {
      assertEquals(Routes.status,"performanceStats is disabled");
    }
  }

  @Test
  @Order(2)
  public void testQueriesNotifyTryCatch() {

    config.performanceStats = false;
    RouteMetric metric = new RouteMetric("GET", "/test", 200, "application/json");
    try {
      notifier.route.notify(metric);
    } finally {
      assertEquals(Routes.status, "performanceStats is disabled");
    }
  }


  @Test
  @Order(3)
  public void testRouteNotifyException() {

    config.performanceStats = true;
    config.projectId = 1;
    notifier.setAPMHost("http://localhost:8080");

    RouteMetric metric = new RouteMetric();

    stubFor(post(urlEqualTo("/api/v5/projects/1/routes-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      notifier.route.notify(metric);
      Thread.sleep(5000);

    } catch (Exception e) {
      assertTrue(false);
    }
    assertEquals(Routes.status, "java.lang.NullPointerException");
  }

  @AfterAll
  public static void closeWireMockServer() {
    wireMockServer.stop();
  }
}
