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
  }
}
