package io.airbrake.javabrake;

//import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

public class TruncatorTest {
  Gson gson = new Gson();

  @Test
  public void testTruncateString() {
    Map<String, Object> map = new HashMap<>();
    map.put("hello", "world");

    Map<String, Object> submap = new HashMap<>();
    submap.put("hello", "world");
    map.put("submap", submap);

    List<String> list = new ArrayList<>();
    list.add("world");
    map.put("list", list);

    Truncator t = new Truncator(0);
    t.maxStringLength = 3;

    map = t.truncateMap(map, 0);
    assertEquals("wor", map.get("hello"));

    // submap = (Map<String, Object>) map.get("submap");
    // assertEquals("wor", submap.get("hello"));

     //list = (List<String>) map.get("list");
    // assertEquals("wor", list.get(0));

    submap = gson.fromJson(gson.toJson(map.get("submap")), new TypeToken<Map<String, Object>>(){}.getType());
    assertEquals("wor", submap.get("hello"));

    list = gson.fromJson(gson.toJson(map.get("list")), new TypeToken<List<Object>>(){}.getType());
    assertEquals("wor", list.get(0));
  }

  @Test
  public void testTruncateMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("k1", "v1");
    map.put("k2", "v2");
    map.put("k3", "v3");
    map.put("k4", "v4");

    Truncator t = new Truncator(0);
    t.maxMapSize = 3;
    map = t.truncateMap(map, 0);
    assertEquals(3, map.size());
  }

  @Test
  public void testTruncateMapStringString() {
    Map<String, Object> map = new HashMap<>();

    Map<String, String> submap = new HashMap<>();
    submap.put("hello", "world");
    map.put("submap", submap);

    Truncator t = new Truncator(0);
    map = t.truncateMap(map, 0);

    // Map<String, Object> v = (Map<String, Object>) map.get("submap");
    // assertEquals("world", v.get("hello"));

    Map<String, Object> v = gson.fromJson(gson.toJson(map.get("submap")), new TypeToken<Map<String, Object>>(){}.getType());
    assertEquals("world", v.get("hello"));
  }

  @Test
  public void testTruncateList() {
    Map<String, Object> map = new HashMap<>();

    List<String> list = new ArrayList<>();
    list.add("v1");
    list.add("v2");
    list.add("v3");
    list.add("v4");
    map.put("list", list);

    // Truncator t = new Truncator(0);
    // t.maxListSize = 3;
    // map = t.truncateMap(map, 0);
    // assertEquals(3, list.size());

    Truncator t = new Truncator(0);
    t.maxListSize = 3;
    map = t.truncateMap(map, 0);

    Object obj = map.get("list");
  
    List<Object> list1 = gson.fromJson(gson.toJson(obj), new TypeToken<List<Object>>(){}.getType());
    assertEquals(3, list1.size());
  }

  @Test
  public void testTruncateDepth() {
    Map<String, Object> map = new HashMap<>();

    Map<String, Object> submap = new HashMap<>();
    map.put("submap", submap);

    Map<String, Object> submap2 = new HashMap<>();
    submap.put("submap2", submap2);

    Map<String, Object> submap3 = new HashMap<>();
    submap2.put("submap3", submap3);

    Truncator t = new Truncator(0);
    t.maxDepth = 2;

    map = t.truncateMap(map, 0);
    // submap = (Map<String, Object>) map.get("submap");
    // submap2 = (Map<String, Object>) submap.get("submap2");

    submap = gson.fromJson(gson.toJson(map.get("submap")), new TypeToken<Map<String, Object>>(){}.getType());
    submap2 = gson.fromJson(gson.toJson(submap.get("submap2")), new TypeToken<Map<String, Object>>(){}.getType());

    String s = (String) submap2.get("submap3");
    assertEquals("[Truncated Map]", s);
  }

  @Test
  public void testTruncateNull() {
    Truncator truncator = new Truncator(0);
    Map<String, Object> output = truncator.truncateMap(null, 0);
    assertEquals(new HashMap<String, Object>(), output);
  }
}
