package javabrake;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class NotifierTest {
  @Test
  public void testReportSync() {
    Notifier notifier = new Notifier(108686, "9fd45149aa4a6fc847a65a4c3f909208");
    notifier.addFilter(
        (Notice notice) -> {
          notice.setContext("environment", "test");
          return notice;
        });

    Notice notice = notifier.reportSync(new IOException("hello from Java"));
    assertNotNull(notice.id);

    assertEquals(notice.errors.size(), 1);
    AirbrakeError err = notice.errors.get(0);
    assertEquals("java.io.IOException", err.type);
    assertEquals("hello from Java", err.message);

    AirbrakeStackRecord record = err.backtrace.get(0);
    assertEquals("testReportSync", record.function);
    assertEquals("test/javabrake/NotifierTest.class", record.file);
    assertEquals(17, record.line);

    String hostname = (String) notice.context.get("hostname");
    assertTrue(hostname != "");
  }
}
