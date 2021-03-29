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
compile 'io.airbrake:javabrake:0.2.1'
```

Maven:

```xml
<dependency>
  <groupId>io.airbrake</groupId>
  <artifactId>javabrake</artifactId>
  <version>0.2.1</version>
</dependency>
```

Ivy:

```xml
<dependency org='io.airbrake' name='javabrake' rev='0.2.1'>
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

Using `notifier` directly:

```java
try {
  do();
} catch (IOException e) {
  notifier.report(e);
}
```

Using `Airbrake` proxy class:

```java
import io.airbrake.javabrake.Airbrake;

try {
  do();
} catch (IOException e) {
  Airbrake.report(e);
}
```

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

Or ignore specific notice:

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

## log4j integration

See https://github.com/airbrake/log4javabrake

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

## Build

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
