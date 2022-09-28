package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Date;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;

public class QueuesTest {
 
    static Notifier notifier;
    static Config config = null;
    static WireMockServer wireMockServer = null;

    @BeforeAll
    public static void init() {
      wireMockServer = new WireMockServer(); // No-args constructor will start on port 8080, no HTTPS
      wireMockServer.start();
      config = new Config();
      config.remoteConfig = false;
      notifier = new Notifier(config);
    }

    @Test
    public void testQueueMetricStartEnd()
    {
        QueueMetric metric = new QueueMetric("test");
        metric.end();
        assertTrue(metric.endTime != null); 
    }

    
    @Test
    public void testQueuesNotifyException() {
       
      config.performanceStats = false;
      QueueMetric metric = new QueueMetric("test");
      try {
        metric.groups.put("redis", (long)24);
        metric.groups.put("sql", (long)4);
        notifier.queues.notify(metric);
      } finally {
        assertEquals(Queues.status,"performanceStats is disabled");
      }
    }

    @Test
    public void testQueueStatsFalse()
    {
      try{
      config.performanceStats = true;
      config.queueStats = false;
      QueueMetric metric = new QueueMetric("test");
      metric.end();
      notifier.queues.notify(metric);
      
    } finally {
      assertEquals(Queues.status,"queueStats is disabled");
    }
    }

    @Test
    public void testQueuesNotifyEnv() {
      try{ 
      config.performanceStats = true;
      config.queueStats = true;
      QueueMetric metric = new QueueMetric("test");
      metric.end();
      notifier.queues.notify(metric);
     
    } finally {
      assertEquals(config.environment,"production");
    }
    }

    @Test
  public void testRoutesNotify() {

    config.performanceStats = true;
    config.environment = "Production";
    config.projectId = 0;
    notifier.setAPMHost("http://localhost:8080");
    QueueMetric metric = new QueueMetric("Test");

    RouteMetric.FLUSH_PERIOD = 50;
    metric.endTime = new Date();

    stubFor(post(urlEqualTo("/api/v5/projects/0/queues-stats")).withHeader("Authorization", containing("Bearer "))
        .willReturn(aResponse().withBody("{}")
            .withStatus(200)));

    try {
      notifier.queues.notify(metric);
      Thread.sleep(5000);
    } catch (Exception e) {
      assertTrue(false);
    }

    assertTrue(true);
  }

    @AfterAll
    public static void closeWireMockServer() {
      wireMockServer.stop();
    }
}
