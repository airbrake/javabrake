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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.tomakehurst.wiremock.WireMockServer;

import net.minidev.json.JSONObject;
import okhttp3.Call;
import okhttp3.Response;
import okio.Buffer;

@TestMethodOrder(OrderAnnotation.class)
public class QueriesTest {

  static WireMockServer wireMockServer = null;
  static Notifier notifier;
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
  public void testPerformanceStatsFalse() {

    config.performanceStats = false;
    try {
      notifier.queries.notify(this.method,
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
      notifier.queries.notify(this.method,
          this.route,
          this.query,
          this.startTime, this.endTime, null, null, 0);
    } finally {
      assertEquals(Queries.status, "queryStats is disabled");
    }

  }

  @Test
  @Order(3)
  public void testQueriesDefaultEnv() {
    try {
      config.performanceStats = true;
      config.queryStats = true;
      config.environment = null;
      notifier = new Notifier(config);
      notifier.queries.notify(this.method,
          this.route,
          this.query,
          this.startTime, this.endTime, null, null, 0);
    } finally {
      assertEquals(config.environment, "production");
    }

  }

  @Test
  @Order(4)
  public void testQueriesNotifyException() {

    config.performanceStats = true;
    config.queryStats = true;
    config.environment = "production";
    config.projectId = 0;
    notifier.setAPMHost("http://localhost:8080");

    stubFor(post(urlEqualTo("/api/v5/projects/0/queries-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      notifier.queries.notify(null,
          null,
          null,
          null, null, null, null, 0);
    } finally {
      assertEquals(Queries.status, "java.lang.NullPointerException");
    }

  }

  @Test
  @Order(5)
  public void testQueryNotifySuccess() {

    config.performanceStats = true;
    config.projectId = 1;
    notifier.setAPMHost("http://localhost:8080");

    stubFor(post(urlEqualTo("/api/v5/projects/1/queries-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{'message':'Success'}")
            .withStatus(200)));

    List<Object> list = new ArrayList<>();
    String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(this.startTime);

    QueryStats queryStats = new QueryStats(this.method, this.route, this.query, date, null, null, 0);
    list.add(queryStats);

    long ms = this.endTime.getTime() - this.startTime.getTime();
    queryStats.add(ms);
    Queries queries = new Queries(config.environment, list);

    OkSender okSender = new OkSender(config);



    Call call = OkSender.okhttp.newCall(okSender.buildAPMRequest(OkSender.gson.toJson(queries), Constant.apmQuery));
    try (Response resp = call.execute()) {

      try {
        Buffer buffer = new Buffer();
        try {
          resp.request().body().writeTo(buffer);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        String json = buffer.readUtf8();
       
        verify(postRequestedFor(urlEqualTo("/api/v5/projects/1/queries-stats"))
        .withRequestBody(equalTo(json)));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      JSONObject res = null;
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
