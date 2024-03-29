package io.airbrake.javabrake;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

class Truncator {
  int maxStringLength = 1024;
  int maxMapSize = 128;
  int maxListSize = 32;
  int maxDepth = 8;
  Gson gson = new Gson();

  Truncator(int level) {
    this.maxStringLength = Math.max(1, this.maxStringLength >> level);
    this.maxMapSize = Math.max(1, this.maxMapSize >> level);
    this.maxListSize = Math.max(1, this.maxListSize >> level);
    this.maxDepth = Math.max(1, this.maxDepth >> level);
  }

  Object truncate(Object obj, int depth) {
    if (obj instanceof String) {
      String s = (String) obj;
      if (s.length() > this.maxStringLength) {
        s = s.substring(0, this.maxStringLength);
      }
      return s;
    }

    if (obj instanceof Map) {
      depth++;
      if (depth > this.maxDepth) {
        return "[Truncated Map]";
      }

      Map<String, Object> map = gson.fromJson(gson.toJson(obj), new TypeToken<Map<String, Object>>(){}.getType());
      return this.truncateMap(map, depth);
    }

    if (obj instanceof List) {
      depth++;
      if (depth > this.maxDepth) {
        return "[Truncated List]";
      }

      List<Object> list = gson.fromJson(gson.toJson(obj), new TypeToken<List<Object>>(){}.getType());

      for (int i = 0; i < list.size(); i++) {
        if (i >= this.maxListSize) {
          list.remove(i);
          continue;
        }

        list.set(i, this.truncate(list.get(i), depth));
      }
      return list;
    }

    return obj;
  }

  Map<String, Object> truncateMap(Map<String, Object> map, int depth) {
    Map<String, Object> dst = new HashMap<>();
    if (map == null) {
      return dst;
    }
    int i = 0;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (i >= this.maxMapSize) {
        map.remove(entry.getKey());
        continue;
      }

      Object value = this.truncate(entry.getValue(), depth);
      dst.put(entry.getKey(), value);

      i++;
    }
    return dst;
  }
}
