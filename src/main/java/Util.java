package javabrake;

import java.util.ArrayList;
import java.lang.StackTraceElement;
import java.lang.Class;
import java.net.URL;

class Util {
  static String[] dirs = System.getProperty("java.class.path").split(":");
  static String[] jars;
  static {
    ArrayList<String> tmp = new ArrayList<>();
    for (String dir : dirs) {
      if (dir.endsWith(".jar")) {
        // Remove jar name from the dir.
        dir = dir.substring(0, dir.lastIndexOf("/") + 1);
        tmp.add(dir);
      }
    }
    jars = new String[tmp.size()];
    jars = tmp.toArray(jars);
  }

  static String getFilepath(String className) {
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

    boolean isJar = filepath.startsWith("jar:");
    if (isJar) {
      filepath = Util.trimLeft(filepath, "jar:");
    }
    filepath = Util.trimLeft(filepath, "file:");

    if (isJar) {
      filepath = Util.trimDirs(filepath, "", Util.jars);
    } else {
      filepath = Util.trimDirs(filepath, "[PROJECT_ROOT]", Util.dirs);
    }

    return filepath;
  }

  static String trimLeft(String s, String substr) {
    if (s.startsWith(substr)) {
      return s.substring(substr.length());
    }
    return s;
  }

  static String trimDirs(String s, String repl, String[] dirs) {
    for (String dir : dirs) {
      if (s.startsWith(dir)) {
        s = s.replace(dir, repl);
        break;
      }
    }
    return s;
  }
}
