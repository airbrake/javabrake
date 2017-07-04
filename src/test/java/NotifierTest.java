package io.airbrake.javabrake;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class NotifierTest {
  Notifier notifier = new Notifier(0, "");
  Throwable exc = new IOException("hello from Java");

  @Test
  public void testBuildNotice() {
    Notice notice = notifier.buildNotice(exc);

    assertEquals(notice.errors.size(), 1);
    NoticeError err = notice.errors.get(0);
    assertEquals("java.io.IOException", err.type);
    assertEquals("hello from Java", err.message);

    NoticeStackRecord record = err.backtrace.get(0);
    assertEquals("<init>", record.function);
    assertEquals("test/io/airbrake/javabrake/NotifierTest.class", record.file);
    assertEquals(9, record.line);

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

    Notice notice = notifier.reportSync(exc);

    String env = (String) notice.context.get("environment");
    assertEquals("test", env);
  }

  @Test
  public void testFilterNull() {
    notifier.addFilter(
        (Notice notice) -> {
          return null;
        });

    Notice notice = notifier.reportSync(exc);
    assertNull(notice);
  }
}
