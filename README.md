Javabrake
=========

[![.github/workflows/test.yml](https://github.com/airbrake/javabrake/actions/workflows/test.yml/badge.svg)](https://github.com/airbrake/javabrake/actions/workflows/test.yml)

<p align="center">
  <img src="https://airbrake-github-assets.s3.amazonaws.com/brand/airbrake-full-logo.png" width="200">
</p>

## Introduction

Javabrake is a Java notifier for Airbrake.

## Installation

Gradle:

```gradle
implementation 'io.airbrake:javabrake:0.3.0'
```

Maven:

```xml
<dependency>
  <groupId>io.airbrake</groupId>
  <artifactId>javabrake</artifactId>
  <version>0.3.0</version>
</dependency>
```

Ivy:

```xml
<dependency org='io.airbrake' name='javabrake' rev='0.3.0'>
  <artifact name='javabrake' ext='pom'></artifact>
</dependency>
```

## Quickstart

Configuration:

```java
import io.airbrake.javabrake.Notifier;
import io.airbrake.javabrake.Config;

Config config = new Config();
config.projectId = 12345;
config.projectKey = "FIXME";
Notifier notifier = new Notifier(config);

notifier.addFilter(
    (Notice notice) -> {
      notice.setContext("environment", "production");
      return notice;
    });
```

## Error Monitoring

### Sending errors to Airbrake

#### Using `notifier` directly

```java
try {
  do();
} catch (IOException e) {
  notifier.report(e);
}
```

#### Using `Airbrake` proxy class

```java
import io.airbrake.javabrake.Airbrake;

try {
  do();
} catch (IOException e) {
  Airbrake.report(e);
}
```

### Sending errors synchronously

By default `report` sends errors asynchronously returning a `Future`, but synchronous API is also available:

```java
import io.airbrake.javabrake.Notice;

Notice notice = Airbrake.reportSync(e);
if (notice.exception != null) {
    logger.info(notice.exception);
} else {
    logger.info(notice.id);
}
```

### Adding custom params

To set custom params you can build and send notice in separate steps:

```java
import io.airbrake.javabrake.Notice;

Notice notice = Airbrake.buildNotice(e);
notice.setContext("component", "mycomponent");
notice.setParam("param1", "value1");
Airbrake.send(notice);
```

You can also set custom params on all reported notices:

```java
notifier.addFilter(
    (Notice notice) -> {
      notice.setParam("myparam", "myvalue");
      return notice;
    });
```

### Linking errors to routes

You can link error notices with the routes by setting the route e.g. /hello and httpMethod e.g. GET, POST in the custom parameters for error notices. For example:

```java
Notice notice = notifier.buildNotice(e);
notice.setContext("route", "route-name");
notice.setContext("httpMethod", "http-method-name");
```

### Ignoring notices

gnore specific notice:

```java
notifier.addFilter(
    (Notice notice) -> {
      if (notice.context.get("environment") == "development") {
          // Ignore notice.
          return null;
      }
      return notice;
    });
```

### Debugging notices

To debug why notices are not sent you can use `onReportedNotice` hook:

```java
notifier.onReportedNotice(
    (notice) -> {
      if (notice.exception != null) {
        logger.info(notice.exception);
      } else {
        logger.info(String.format("notice id=%s url=%s", notice.id, notice.url));
      }
    });
```

## Performance Monitoring

You can read more about our [Performance Monitoring offering in our docs][docs/performance].

### Sending route stats

`notifier.routes.notify` allows sending route stats to Airbrake. You can also use this API manually:

```java
import io.airbrake.javabrake.RouteMetric;

RouteMetric metric = new RouteMetric(request.getMethod(), request.getRequestURI());
metric.statusCode = response.getStatus();
metric.contentType = response.getContentType();
metric.endTime = new Date();

notifier.routes.notify(metric);
```

### Sending route breakdowns

`notifier.routes.notify` allows sending performance breakdown stats
to Airbrake. You can use this API manually:

```java
import io.airbrake.javabrake.RouteMetric;

RouteMetric metric = new RouteMetric(request.getMethod(), request.getRequestURI());

metric.startSpan("span1 name", new Date());
try {
	do();
} catch (Exception e) {
	e.printStackTrace();
}
metric.endSpan("span1 name", new Date());

metric.startSpan("span2 name", new Date());
try {
  do();
} catch (Exception e) {
	e.printStackTrace();
}
metric.endSpan("span2 name", new Date());		
metric.end();

metric.statusCode = response.getStatus();
metric.contentType = response.getContentType();

notifier.routes.notify(metric);
```

### Sending query stats

`notifier.queries.notify` allows sending SQL query stats to Airbrake. You can also use this API manually:

```java
Date startTime = new Date();
try
{
  do();
}catch(
Exception e)
{
  e.printStackTrace();
}
Date endTime = new Date();

notifier.queries.notify(request.getMethod(),request.getRequestURI()
,"SELECT * FROM foos",startTime,endTime);
```

### Sending queue stats

`notifier.queues.notify` allows sending queue (job) stats to Airbrake. You can also use this API manually:

```java
import io.airbrake.javabrake.QueueMetric;

QueueMetric metric = new QueueMetric("foo_queue");

metric.startSpan("span1 name", new Date());
try {
    do();
} catch (Exception e) {
    e.printStackTrace();
}
metric.endSpan("span1 name", new Date());

metric.startSpan("span2 name", new Date());
try {
  do();
} catch (Exception e) {
  e.printStackTrace();
}
metric.endSpan("span2 name", new Date());
metric.end();

notifier.queues.notify(metric);

```

For more information visit [docs](https://docs.airbrake.io/docs/platforms/java/)

## log4j2 integration

See https://github.com/airbrake/log4javabrake2

## logback integration

See https://github.com/airbrake/logback

## HTTP proxy

javabrake uses [OkHttp](http://square.github.io/okhttp/) as an HTTP client. So in order to use proxy all you have to do is to configure OkHttpClient:

```java
import java.net.InetSocketAddress;

import okhttp3.OkHttpClient;
import okhttp3.Proxy;

import io.airbrake.javabrake.OkSender;

Proxy proxy = new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved("192.168.1.105", 8081);
OkHttpClient httpClient =
    new OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(3000, TimeUnit.MILLISECONDS)
        .writeTimeout(3000, TimeUnit.MILLISECONDS)
        .proxy(proxy)
        .build();
OkSender.setOkHttpClient(httpClient);
```

## Notifier release instrucitons

### A note on Java version
Make sure you build and release this notifier with open-jdk-8, one way to manage your local java version is using [asdf](https://asdf-vm.com). You can install this tool via homebrew:
```
brew install asdf
```
Then install open-jdk-8 and set it as JAVA home before running any of the `./gradlew` commands:
```
asdf plugin add java
asdf install java adoptopenjdk-8.0.312+7
export JAVA_HOME=$HOME/.asdf/installs/java/adoptopenjdk-8.0.312+7
```

### Build

```shell
./gradlew build
```

Upload to JCentral:

```shell
./gradlew bintrayUpload
```

Upload to Maven Central:

```shell
./gradlew uploadArchives
./gradlew closeAndReleaseRepository
```

Usefull links:
 - http://central.sonatype.org/pages/gradle.html
 - http://central.sonatype.org/pages/releasing-the-deployment.html


[docs/performance]: https://docs.airbrake.io/docs/overview/apm/#monitoring-java-apps