package io.airbrake.javabrake;

import java.util.ArrayList;
import java.lang.StackTraceElement;
import java.lang.Class;
import java.net.URL;

class ClassUtil {
  static String[] dirs = System.getProperty("java.class.path").split(":");

  static {
    for (int i = 0; i < dirs.length; i++) {
      String dir = dirs[i];
      dirs[i] = dir.substring(0, dir.lastIndexOf("/") + 1);
    }
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
    if (loader == null) { // built-in class
      return className;
    }

    URL url = loader.getResource(className.replace('.', '/') + ".class");
    if (url == null) {
      return className;
    }

    String filepath = url.toString();

    filepath = this.trimLeft(filepath, "jar:");
    filepath = this.trimLeft(filepath, "file:");
    filepath = this.trimDirs(filepath, "", ClassUtil.dirs);

    return filepath;
  }

  String trimLeft(String s, String substr) {
    if (s.startsWith(substr)) {
      return s.substring(substr.length());
    }
    return s;
  }

  String trimDirs(String s, String repl, String[] dirs) {
    for (String dir : dirs) {
      if (s.startsWith(dir)) {
        s = s.replace(dir, repl);
        break;
      }
    }
    return s;
  }
}
