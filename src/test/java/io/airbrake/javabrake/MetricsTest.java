package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class MetricsTest {
    @Test
    public void test()
    {
        Metrics metric = new Metrics();
        metric.startSpan("c", new Date());
        try {
            Thread.sleep(5000);

            metric.endSpan("c", new Date());

            Thread.sleep(5000);
           
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        metric.startSpan("c1", new Date());
       
        try {
            
            Thread.sleep(1000);
          
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        metric.endSpan("c1", new Date());
        metric.end();

        assertTrue((metric.spans.get("c").endTime.getTime()-metric.spans.get("c").startTime.getTime())==metric.spans.get("c").dur);
        assertTrue((metric.spans.get("c1").endTime.getTime()-metric.spans.get("c1").startTime.getTime())==metric.spans.get("c1").dur);
        assertTrue((metric.endTime.getTime()-metric.startTime.getTime())>10000);
    }

}
