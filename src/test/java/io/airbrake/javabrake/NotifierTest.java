package io.airbrake.javabrake;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import com.github.tomakehurst.wiremock.WireMockServer;
//import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class NotifierTest {
  //@Rule public WireMockRule wireMockRule = new WireMockRule();
  static WireMockServer wireMockServer = null;
  static Notifier notifier;
  Throwable exc = new IOException("hello from Java");

  @BeforeAll
  public static void init() {
    wireMockServer = new WireMockServer(); //No-args constructor will start on port 8080, no HTTPS
    wireMockServer.start();
    Config config = new Config();
    config.remoteConfig = false;
    notifier = new Notifier(config);
  }

  @Test
  public void testBuildNotice() {
    Notice notice = NotifierTest.notifier.buildNotice(this.exc);

    assertEquals(notice.errors.size(), 1);
    NoticeError err = notice.errors.get(0);
    assertEquals("java.io.IOException", err.type);
    assertEquals("hello from Java", err.message);

    NoticeStackFrame frame = err.backtrace[0];
    assertEquals("<init>", frame.function);
    String hostname = (String) notice.context.get("hostname");
    assertTrue(hostname != "");
  }

  @Test
  public void testFilterData() {
    Config config = new Config();
    config.remoteConfig = false;
    Notifier notifier = new Notifier(config);

    notifier.addFilter(
        (Notice notice) -> {
          notice.setContext("environment", "test");
          return notice;
        });

    Notice notice = notifier.reportSync(this.exc);
    assertNotNull(notice.exception);
    assertEquals(
        "java.io.IOException: unauthorized: project id or key are wrong",
        notice.exception.toString());

    String env = (String) notice.context.get("environment");
    assertEquals("test", env);
  }

  @Test
  public void testReportAsync() {
    try {
      NotifierTest.notifier.report(this.exc).get();
      fail("expected an exception");
    } catch (Throwable e) {
      e = e.getCause();
      assertEquals("java.io.IOException: unauthorized: project id or key are wrong", e.toString());
    }
  }

  @Test
  public void testReportServerDown() {
    NotifierTest.notifier.setErrorHost("https://google.com");
    Notice notice = NotifierTest.notifier.reportSync(this.exc);
    assertNotNull(notice.exception);
    assertEquals(
        "com.google.gson.JsonSyntaxException: java.lang.IllegalStateException: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path $",
        notice.exception.toString());
  }

  @Test
  public void testFilterNull() {
    NotifierTest.notifier.addFilter(
        (Notice notice) -> {
          return null;
        });

    Notice notice = NotifierTest.notifier.reportSync(this.exc);
    assertNull(notice);
  }

  @Test
  public void testRateLimit() {
    
    String apiURL = "/api/v3/projects/0/notices";

    notifier.setErrorHost("http://localhost:8080");
   // long utime = System.currentTimeMillis() / 1000L;
    stubFor(
        post(urlEqualTo(apiURL))
            .willReturn(
                aResponse()
                    .withStatus(429)
                    .withHeader("Content-Type", "application/json")
                    .withHeader("X-RateLimit-Delay", "1000")
                    .withBody("{}")));

    for (int i = 0; i < 2; i++) {
      Notice notice = notifier.reportSync(this.exc);
      assertNotNull(notice.exception);
      assertEquals("java.io.IOException: IP is rate limited", notice.exception.toString());
    }

    verify(1, postRequestedFor(urlEqualTo(apiURL)));

  }

  @AfterAll
  public static void closeWireMockServer()
  {
    wireMockServer.stop();
  }
}
