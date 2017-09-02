# Java notifier for Airbrake

[![Build Status](https://travis-ci.org/airbrake/javabrake.svg?branch=master)](https://travis-ci.org/airbrake/javabrake)

<img src="http://f.cl.ly/items/0u0S1z2l3Q05371C1C0I/java%2009.19.32.jpg" width=800px>

## Installation

Gradle:

```gradle
compile 'io.airbrake:javabrake:0.1.2'
```

Maven:

```xml
<dependency>
  <groupId>io.airbrake</groupId>
  <artifactId>javabrake</artifactId>
  <version>0.1.2</version>
  <type>pom</type>
</dependency>
```

Ivy:

```xml
<dependency org='io.airbrake' name='javabrake' rev='0.1.2'>
  <artifact name='javabrake' ext='pom'></artifact>
</dependency>
```

## Quickstart

Configuration:

```java
import io.airbrake.javabrake.Notifier;

int projectId = 12345;
String projectKey = "abcdefg";
Notifier notifier = new Notifier(projectId, projectKey);

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

``` java
import io.airbrake.javabrake.Airbrake;

try {
  do();
} catch (IOException e) {
  Airbrake.report(e);
}
```

Setting custom params:

```java
notifier.addFilter(
    (Notice notice) -> {
      notice.setParam("myparam", "myvalue");
      return notice;
    });
```

Debugging why notices are not sent:

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

## Build

```sh
gradle build
```
