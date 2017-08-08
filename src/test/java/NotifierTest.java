package io.airbrake.javabrake;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class NotifierTest {
  Notifier notifier = new Notifier(0, "");

  @Test
  public void testBuildNotice() {
    Notice notice = notifier.buildNotice(new IOException("hello from Java"));

    assertEquals(notice.errors.size(), 1);
    AirbrakeError err = notice.errors.get(0);
    assertEquals("java.io.IOException", err.type);
    assertEquals("hello from Java", err.message);

    AirbrakeStackRecord record = err.backtrace.get(0);
    assertEquals("testBuildNotice", record.function);
    assertEquals("test/io/airbrake/javabrake/NotifierTest.class", record.file);
    assertEquals(12, record.line);

    String hostname = (String) notice.context.get("hostname");
    assertTrue(hostname != "");
  }

  @Test
  public void testFilterData() {
    notifier.addFilter(
        (Notice notice) -> {
          notice.setContext("environment", "test");
          return notice;
        });

    Notice notice = notifier.buildNotice(new IOException("hello from Java"));

    String env = (String) notice.context.get("environment");
    assertEquals("test", env);
  }

  @Test
  public void testFilterNull() {
    notifier.addFilter(
        (Notice notice) -> {
          return null;
        });

    Notice notice = notifier.buildNotice(new IOException("hello from Java"));
    assertNull(notice);
  }
}
