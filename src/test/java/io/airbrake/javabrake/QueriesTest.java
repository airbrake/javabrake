package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
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
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@TestMethodOrder(OrderAnnotation.class)
@TestClassOrder(org.junit.jupiter.api.ClassOrderer.OrderAnnotation.class)
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
  public void testQueryNotifySuccess() {

    config.performanceStats = true;
    config.projectId = 1;
    notifier.setAPMHost("http://localhost:8080");

    List<Object> list = new ArrayList<>();
    String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(this.startTime);

    QueryStats queryStats = new QueryStats(this.method, this.route, this.query, date, null, null, 0);
    list.add(queryStats);

    long ms = this.endTime.getTime() - this.startTime.getTime();
    queryStats.add(ms);

    Queries queries = new Queries(config.environment, list);

    String json = "{\"environment\":\"${json-unit.any-string}\",\"queries\":[{\"method\":\"${json-unit.any-string}\",\"route\":\"${json-unit.any-string}\","
        +"\"query\":\"${json-unit.any-string}\","
        +"\"time\":\"${json-unit.any-string}\",\"line\":\"${json-unit.any-number}\",\"count\":\"${json-unit.any-number}\",\"sum\":\"${json-unit.any-number}\","
        +"\"sumsq\":\"${json-unit.any-number}\",\"tdigest\":\"${json-unit.any-string}\"}]}";

    String queriesJson = OkSender.gson.toJson(queries);

    assertThatJson(queriesJson).isEqualTo(json);

    stubFor(post(urlEqualTo("/api/v5/projects/1/queries-stats")).withHeader("Authorization", containing("Bearer "))
        .withRequestBody(equalToJson(queriesJson, true, true))
        .willReturn(aResponse().withBody("{'message':'Success'}")
            .withStatus(200)));

    OkSender okSender = new OkSender(config);

    Call call = OkSender.okhttp.newCall(okSender.buildAPMRequest(queriesJson, Constant.apmQuery));
    try (Response resp = call.execute()) {
      JSONObject res = null;

      try {
        res = OkSender.gson.fromJson(resp.body().string(), JSONObject.class);
        resp.body().close();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      assertEquals(res.get("message"), "Success");
      resp.close();
    } catch (IOException e) {

    }
  }

  @Test
  @Order(5)
  public void testQueryBacklog() {

    config.performanceStats = true;
    config.projectId = 1;
    String apiURL = "/api/v5/projects/1/queries-stats";

    notifier.setAPMHost("http://localhost:8080");

    stubFor(
        post(urlEqualTo(apiURL)).withHeader("Authorization", containing("Bearer "))
            .willReturn(aResponse().withBody("{'message':'Error'}")
                .withStatus(404)));

    APMBackLog.start();
    APMBackLog.apmBackLogList.clear();
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    notifier.queries.notify(this.method, this.route, this.query, this.startTime, this.endTime, null, null, 0);
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @AfterAll
  public static void closeWireMockServer() {
    wireMockServer.stop();
  }
}
