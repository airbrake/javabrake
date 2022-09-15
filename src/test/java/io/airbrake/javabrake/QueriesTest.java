package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import java.io.IOException;
import java.util.Date;
import com.github.tomakehurst.wiremock.WireMockServer;

@TestMethodOrder(OrderAnnotation.class)
public class QueriesTest {

  static WireMockServer wireMockServer = null;
  static Notifier notifier;
  Throwable exc = new IOException("hello from Java");
  static Config config = null;

  Date startTime = new Date();
  Date endTime = new Date();
  String query = "SELECT * FROM foos";
  String method = "GET";
  String route = "/test";

  @BeforeAll
  public static void init() {
    wireMockServer = new WireMockServer(); // No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();
    config = new Config();
    config.remoteConfig = false;
    notifier = new Notifier(config);
  }

  @Test
  @Order(1)
  public void testQueriesNotifyTryCatch() {

    config.performanceStats = false;
    try {
      notifier.query.notify(this.method,
          this.route,
          this.query,
          this.startTime, this.endTime, null, null, 0);
    } finally {
      assertEquals(Queries.status, "performanceStats is disabled");
    }
  }

  @Test
  @Order(2)
  public void testQueriesStatsFalse() {
    config.performanceStats = true;
    config.queryStats = false;
    try {
      notifier.query.notify(this.method,
          this.route,
          this.query,
          this.startTime, this.endTime, null, null, 0);
    } finally {
      assertEquals(Queries.status, "queryStats is disabled");
    }

  }

  @Test
  @Order(3)
  public void testQueriesNotifyException() {
    try {
      config.performanceStats = true;
      config.queryStats = true;
      config.environment = null;
      notifier = new Notifier(config);
      notifier.query.notify(this.method,
          this.route,
          this.query,
          this.startTime, this.endTime, null, null, 0);
    } finally {
      assertEquals(config.environment, "production");
    }

  }

  @Test
  @Order(4)
  public void testQueriesNotify() {

    config.performanceStats = true;
    config.queryStats = true;
    config.environment = "Production";
    config.projectId = 0;
    notifier.setAPMHost("http://localhost:8080");

    Metrics.FLUSH_PERIOD = 50;

    stubFor(post(urlEqualTo("/api/v5/projects/0/queries-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      notifier.query.notify(this.method,
          this.route,
          this.query,
          this.startTime, this.endTime, null, null, 0);
    } finally {
      assertEquals(Queries.status, null);
    }

  }

  @AfterAll
  public static void closeWireMockServer() {
    wireMockServer.stop();
  }
}
