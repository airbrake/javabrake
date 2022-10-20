package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.github.tomakehurst.wiremock.WireMockServer;
import net.minidev.json.JSONObject;
import okhttp3.Call;
import okhttp3.Response;
import okio.Buffer;

public class QueuesTest {

  static Notifier notifier;
  static Config config = null;
  static WireMockServer wireMockServer = null;

  @BeforeAll
  public static void init() {
    wireMockServer = new WireMockServer(); // No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();
    config = new Config();
    config.remoteConfig = false;
    notifier = new Notifier(config);
  }

  @Test
  public void testQueueMetricStartEnd() {
    QueueMetric metric = new QueueMetric("test");
    metric.end();
    assertTrue(metric.endTime != null);
  }

  @Test
  public void testPerformanceStatsFalse() {

    config.performanceStats = false;
    QueueMetric metric = new QueueMetric("test");
    try {
      metric.groups.put("redis", (long) 24);
      metric.groups.put("sql", (long) 4);
      notifier.queues.notify(metric);
    } finally {
      assertEquals(Queues.status, "performanceStats is disabled");
    }
  }

  @Test
  public void testQueueStatsFalse() {
    try {
      config.performanceStats = true;
      config.queueStats = false;
      QueueMetric metric = new QueueMetric("test");
      metric.end();
      notifier.queues.notify(metric);

    } finally {
      assertEquals(Queues.status, "queueStats is disabled");
    }
  }

  @Test
  public void testQueuesNotifyEnv() {
    try {
      config.performanceStats = true;
      config.queueStats = true;
      QueueMetric metric = new QueueMetric("test");
      metric.end();
      notifier.queues.notify(metric);

    } finally {
      assertEquals(config.environment, "production");
    }
  }

  @Test
  public void testQueueNotifyException() {

    config.performanceStats = true;
    config.environment = "production";
    config.projectId = 0;
    notifier.setAPMHost("http://localhost:8080");
    // QueueMetric metric = new QueueMetric("Test");

    stubFor(post(urlEqualTo("/api/v5/projects/0/queues-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      notifier.queues.notify(null);
    } finally {
      assertEquals(Queues.status, "java.lang.NullPointerException");
    }
  }

  public QueueStats getRouteStats() {
    QueueMetric metric = new QueueMetric("test");
    metric.end();

    Metrics.FLUSH_PERIOD = 5;
    metric.endTime = new Date();

    String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(metric.startTime);
    QueueStats queueStats = new QueueStats(metric.queue, date);

    long ms = metric.endTime.getTime() - metric.startTime.getTime();
    queueStats.addGroups(ms, metric.groups);
    return queueStats;
  }

  @Test
  public void testQueueNotifySuccess() {

    config.performanceStats = true;
    config.projectId = 1;
    notifier.setAPMHost("http://localhost:8080");

    stubFor(post(urlEqualTo("/api/v5/projects/1/queues-stats")).withHeader("Authorization", containing("Bearer "))

        .willReturn(aResponse().withBody("{'message':'Success'}")
            .withStatus(200)));

    List<Object> list = new ArrayList<>();
    list.add(getRouteStats());
    Queues queues = new Queues(Notifier.config.environment, list);

    OkSender okSender = new OkSender(config);

    Call call = OkSender.okhttp.newCall(okSender.buildAPMRequest(OkSender.gson.toJson(queues), Constant.apmQueue));
    try (Response resp = call.execute()) {

      JSONObject res = null;

      try {
        Buffer buffer = new Buffer();
        try {
          resp.request().body().writeTo(buffer);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        String json = buffer.readUtf8();
        
        verify(postRequestedFor(urlEqualTo("/api/v5/projects/1/queues-stats"))
          .withRequestBody(equalTo(json)));


      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      try {
        res = OkSender.gson.fromJson(resp.body().string(), JSONObject.class);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      assertEquals(res.get("message"), "Success");
    } catch (IOException e) {
    }
  }

  @AfterAll
  public static void closeWireMockServer() {
    wireMockServer.stop();
  }
}
