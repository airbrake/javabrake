package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.assertj.core.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import java.util.HashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.WireMockServer;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;

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
  
  private static ListAppender<ILoggingEvent> appender;
  private static Logger appLogger = (Logger) LoggerFactory.getLogger(PollTask.class);

  @BeforeAll
  public static void init() {
    appender = new ListAppender<>();
    appender.start();
    appLogger.addAppender(appender);
    
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

    // SettingsData data = new SettingsData(0, new RemoteConfigJSON());
    // HttpUrl.Builder httpBuilder = HttpUrl.parse(data.configRoute("http://localhost:8080/some/thing"))
    //     .newBuilder();
    // for (HashMap.Entry<String, String> param : NOTIFIER_INFO.entrySet()) {
    //   httpBuilder.addQueryParameter(param.getKey(), param.getValue());
    // }
    // OkHttpClient client = new OkHttpClient.Builder().build();

    // Request request = new Request.Builder()
    //     .url(httpBuilder.build())
    //     .build();

    // try {
    //   Response response = client.newCall(request).execute();

    //   responseString = response.body().string();
    // } catch (Exception e) {
    //   // TODO Auto-generated catch block
    //   // e.printStackTrace();
    //   assertEquals(SocketTimeoutException.class, e.getClass());
    // }
    // assertEquals(responseString, "");

    PollTask oPollTask = new PollTask(0, "http://localhost:8080/some/thing", config, asyncSender, syncSender);
   
    oPollTask.run();
  
    //assertEquals(oPollTask.exceptionMessage.toLowerCase(), "read timed out");
    assertThat(appender.list).extracting("message").contains("timeout");
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
    appLogger.detachAppender(appender);
  wireMockServer.stop();
  }
}
