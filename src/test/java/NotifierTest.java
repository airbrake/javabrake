package javabrake;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;

public class NotifierTest {
  @Test public void testReport() {
    Notifier notifier = new Notifier(108686, "9fd45149aa4a6fc847a65a4c3f909208");
    notifier.addFilter((Notice notice) -> {
        notice.setContext("environment", "test");
        return notice;
      });

    Notice notice = notifier.reportSync(new IOException("hello from Java"));
    assertNotNull(notice.id);

    assertEquals(notice.errors.size(), 1);
    AirbrakeError err = notice.errors.get(0);
    assertEquals(err.type, "java.io.IOException");
    assertEquals(err.message, "hello from Java");

    AirbrakeStackRecord record = err.backtrace.get(0);
    assertEquals(record.function, "testReport");
    assertEquals(record.file, "[PROJECT_ROOT]/javabrake/NotifierTest.class");
    assertEquals(record.line, 15);
  }
}
