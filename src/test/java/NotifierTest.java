package io.airbrake.javabrake;

import static org.junit.Assert.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.junit.Test;
import java.io.IOException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;

public class NotifierTest {
  @Rule public WireMockRule wireMockRule = new WireMockRule();

  Notifier notifier = new Notifier(new Config());
  Throwable exc = new IOException("hello from Java");

  @Test
  public void testBuildNotice() {
    Notice notice = this.notifier.buildNotice(this.exc);

    assertEquals(notice.errors.size(), 1);
    NoticeError err = notice.errors.get(0);
    assertEquals("java.io.IOException", err.type);
    assertEquals("hello from Java", err.message);

    NoticeStackFrame frame = err.backtrace[0];
    assertEquals("<init>", frame.function);
    assertEquals("test/io/airbrake/javabrake/NotifierTest.class", frame.file);
    assertEquals(15, frame.line);

    String hostname = (String) notice.context.get("hostname");
    assertTrue(hostname != "");
  }

  @Test
  public void testFilterData() {
    this.notifier.addFilter(
        (Notice notice) -> {
          notice.setContext("environment", "test");
          return notice;
        });

    Notice notice = this.notifier.reportSync(this.exc);
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
      this.notifier.report(this.exc).get();
      fail("expected an exception");
    } catch (Throwable e) {
      e = e.getCause();
      assertEquals("java.io.IOException: unauthorized: project id or key are wrong", e.toString());
    }
  }

  @Test
  public void testReportServerDown() {
    this.notifier.setHost("https://google.com");
    Notice notice = this.notifier.reportSync(this.exc);
    assertNotNull(notice.exception);
    assertEquals(
        "com.google.gson.JsonParseException: Expecting object found: \"<!DOCTYPE\"",
        notice.exception.toString());
  }

  @Test
  public void testFilterNull() {
    this.notifier.addFilter(
        (Notice notice) -> {
          return null;
        });

    Notice notice = this.notifier.reportSync(this.exc);
    assertNull(notice);
  }

  @Test
  public void testRateLimit() {
    String apiURL = "/api/v3/projects/0/notices";

    notifier.setHost("http://localhost:8080");
    long utime = System.currentTimeMillis() / 1000L;
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
}
