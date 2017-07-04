package javabrake;

import java.lang.StackTraceElement;
import java.lang.Class;
import java.net.URL;

class AirbrakeStackRecord {
  String function;
  String file;
  int line;

  AirbrakeStackRecord(StackTraceElement el) {
    this.function = el.getMethodName();
    this.file = this.getFilepath(el.getClassName());
    this.line = el.getLineNumber();
  }

  String getFilepath(String className) {
    Class<?> cls;
    try {
      cls = Class.forName(className);
    } catch (ClassNotFoundException e) {
      return className;
    }

    ClassLoader loader;
    try {
      loader = cls.getClassLoader();
    } catch (SecurityException e) {
      return className;
    }
    if (loader == null) { // primitive
      return className;
    }

    URL url = loader.getResource(className.replace('.', '/') + ".class");
    if (url == null) {
      return className;
    }
    return url.toString();
  }
}
