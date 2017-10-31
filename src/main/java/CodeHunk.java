package io.airbrake.javabrake;

import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

class CodeHunk {
  Map<String, Map<Integer, String>> cache =
      Collections.synchronizedMap(new LruCache<String, Map<Integer, String>>(1000));

  Map<Integer, String> get(String file, int line) {
    String cacheKey = file + line;
    if (this.cache.containsKey(cacheKey)) {
      return this.cache.get(cacheKey);
    }

    Map<Integer, String> lines = _get(file, line);
    this.cache.put(cacheKey, lines);
    return lines;
  }

  Map<Integer, String> _get(String file, int line) {
    int start = line - 2;
    int end = line + 2;

    int i = 0;
    Map<Integer, String> lines = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      while (true) {
        i++;

        String text;
        try {
          text = br.readLine();
        } catch (IOException e) {
          return null;
        }

        if (text == null) {
          break;
        }

        if (i < start) {
          continue;
        }
        if (i > end) {
          break;
        }

        lines.put(i, text);
      }
    } catch (IOException e) {
      return null;
    }

    return lines;
  }
}
