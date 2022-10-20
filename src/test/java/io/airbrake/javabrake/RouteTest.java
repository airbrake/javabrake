package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import com.github.tomakehurst.wiremock.WireMockServer;
import net.minidev.json.JSONObject;
import okhttp3.Call;
import okhttp3.Response;
import okio.Buffer;

@TestMethodOrder(OrderAnnotation.class)
public class RouteTest {
  static WireMockServer wireMockServer = null;
  static Notifier notifier;
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
    RouteMetric metric = new RouteMetric("GET", "/test");

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
  public void testPerformanceStatsFalse() {

    config.performanceStats = false;
    RouteMetric metric = new RouteMetric("GET", "/test");
    metric.statusCode = 200;
    metric.contentType = "application/json";
    try {
      notifier.routes.notify(metric);
    } finally {
      assertEquals(Routes.status, "performanceStats is disabled");
    }
  }

  @Test
  @Order(2)
  public void testRoutesDefaultEnv() {

    config.performanceStats = true;
    config.environment = null;
    RouteMetric metric = new RouteMetric("GET", "/test");
    metric.statusCode = 200;
    metric.contentType = "application/json";
    try {
      notifier.routes.notify(metric);
    } finally {
      assertEquals(config.environment, "production");
    }
  }

  @Test
  @Order(3)
  public void testRouteNotifyException() {

    config.performanceStats = true;
    config.projectId = 1;
    notifier.setAPMHost("http://localhost:8080");

    stubFor(post(urlEqualTo("/api/v5/projects/1/routes-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      notifier.routes.notify(null);

    } finally {
      assertEquals(Routes.status, "java.lang.NullPointerException");
    }
  }

  @Test
  @Order(4)
  public void testRouteNotifySuccess() {

    config.performanceStats = true;
    config.projectId = 1;
    notifier.setAPMHost("http://localhost:8080");

    stubFor(post(urlEqualTo("/api/v5/projects/1/routes-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{'message':'Success'}")
            .withStatus(200)));

    List<Object> routeList = new ArrayList<>();
    routeList.add(getRouteStats());
    Routes routes = new Routes(Notifier.config.environment, routeList);

    OkSender okSender = new OkSender(config);

    Call call = OkSender.okhttp.newCall(okSender.buildAPMRequest(OkSender.gson.toJson(routes), Constant.apmRoute));
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
       
        verify(postRequestedFor(urlEqualTo("/api/v5/projects/1/routes-stats"))
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
