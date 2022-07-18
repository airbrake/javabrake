package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import java.util.HashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.github.tomakehurst.wiremock.WireMockServer;

public class PollTaskTest {

   static WireMockServer wireMockServer = null;

  final private static HashMap<String, String> NOTIFIER_INFO;
  static {
    NOTIFIER_INFO = new HashMap<String, String>();
    NOTIFIER_INFO.put("notifier_name", Notice.notifierInfo.get("name"));
    NOTIFIER_INFO.put("notifier_version", Notice.notifierInfo.get("version"));
    NOTIFIER_INFO.put("os", System.getProperty("os.name") + "/" + System.getProperty("os.version"));
    NOTIFIER_INFO.put("language", "Java/" + System.getProperty("java.version"));
  }

  @BeforeAll
  public static void init() {

  wireMockServer = new WireMockServer(); // No-args constructor will start on port 8080, no HTTPS

  wireMockServer.start();
  }


  @Test
  public void testPollTaskEmptyBody() {
    Config config = new Config();
    config.remoteConfig = true;
    
    AsyncSender asyncSender = new OkAsyncSender(config);
    SyncSender syncSender = new OkSyncSender(config);

    String url = "/some/thing/2020-06-18/config/0/config.json?os="+NOTIFIER_INFO.get("os").replaceAll("/", "%2F").replaceAll(" ", "%20")+
    "&notifier_version="+NOTIFIER_INFO.get("notifier_version")+"&language="+NOTIFIER_INFO.get("language").replaceAll("/", "%2F").replaceAll(" ", "%20")+"&notifier_name="+NOTIFIER_INFO.get("notifier_name");
  
  
    stubFor(get(urlEqualTo(url)).
    withQueryParam("os", containing("a")).
    withQueryParam("language", containing("a")).
    withQueryParam("notifier_version", containing("0.2")).
    withQueryParam("notifier_name", containing("a"))
    .willReturn(aResponse()
    .withStatus(200)));
         
    PollTask oPollTask = new PollTask(0, "http://localhost:8080/some/thing", config, asyncSender, syncSender);

    oPollTask.run();
  

    assertTrue(oPollTask.getErrorHost().equals(Config.DEFAULT_ERROR_HOST));
  }
  
  @Test
  public void testPolltaskTimeout() {
    Config config = new Config();
    config.remoteConfig = true;


    String url = "/some/thing/2020-06-18/config/0/config.json?os="+NOTIFIER_INFO.get("os").replaceAll("/", "%2F").replaceAll(" ", "%20")+
  "&notifier_version="+NOTIFIER_INFO.get("notifier_version")+"&language="+NOTIFIER_INFO.get("language").replaceAll("/", "%2F").replaceAll(" ", "%20")+"&notifier_name="+NOTIFIER_INFO.get("notifier_name");

      AsyncSender asyncSender = new OkAsyncSender(config);
    SyncSender syncSender = new OkSyncSender(config);
    
     stubFor(get(urlEqualTo(url)).
     withQueryParam("os", containing("a")).
     withQueryParam("language", containing("a")).
     withQueryParam("notifier_version", containing("0.2")).
     withQueryParam("notifier_name", containing("a")).
     willReturn(aResponse().withFixedDelay(30000)));

    PollTask oPollTask = new PollTask(0, "http://localhost:8080/some/thing", config, asyncSender, syncSender);
   
    oPollTask.run();
  
    assertTrue(oPollTask.getErrorHost().equals(Config.DEFAULT_ERROR_HOST));
  }


  @Test
  public void testPollTaskSuccess() {

    
    Config config = new Config();
    config.remoteConfig = true;
    
    AsyncSender asyncSender = new OkAsyncSender(config);
    SyncSender syncSender = new OkSyncSender(config);

    String url = "/some/thing/2020-06-18/config/0/config.json?os="+NOTIFIER_INFO.get("os").replaceAll("/", "%2F").replaceAll(" ", "%20")+
    "&notifier_version="+NOTIFIER_INFO.get("notifier_version")+"&language="+NOTIFIER_INFO.get("language").replaceAll("/", "%2F").replaceAll(" ", "%20")+"&notifier_name="+NOTIFIER_INFO.get("notifier_name");
  
  
    stubFor(get(urlEqualTo(url)).
    withQueryParam("os", containing("a")).
    withQueryParam("language", containing("a")).
    withQueryParam("notifier_version", containing("0.2")).
    withQueryParam("notifier_name", containing("a"))
    .willReturn(aResponse()
    .withStatus(200).withBody("{\"project_id\":413949,\"updated_at\":1650979936,\"poll_sec\":0,\"config_route\":\"2020-06-18/config/413949/config.json\",\"settings\":[{\"name\":\"errors\",\"enabled\":true,\"endpoint\":\"test.io\"},{\"name\":\"apm\",\"enabled\":true,\"endpoint\":null}]}")));
      
    PollTask oPollTask = new PollTask(0, "http://localhost:8080/some/thing", config, asyncSender, syncSender);

    oPollTask.run();
    
    assertTrue(oPollTask.getErrorHost().contains("test.io"));
  }

  @AfterAll
  public static void closeWireMockServer() {
  wireMockServer.stop();
  }
}
