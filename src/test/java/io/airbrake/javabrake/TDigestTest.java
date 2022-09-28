package io.airbrake.javabrake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TDigestTest {
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    
    @Test
   public void testTdigestInitialisation()
   {
    TdigestStat stat = new TdigestStat();
    stat.tdigest = stat.getData();
    assertEquals(gson.toJson(stat), "{\"count\":0,\"sum\":0.0,\"sumsq\":0.0,\"tdigest\":\"AAAAAkAkAAAAAAAAAAAAAA==\"}");
   }

   @Test
   public void testTdigestAddMS()
   {
    TdigestStat stat = new TdigestStat();
    stat.add(10000);
    assertEquals(gson.toJson(stat), "{\"count\":1,\"sum\":10000.0,\"sumsq\":1.0E8,\"tdigest\":\"AAAAAkAkAAAAAAAAAAAAAUYcQAABAAAA\"}");
   }

   @Test
   public void testTdigestStatGroupInitialisation()
   {
    TdigestStat stat = new TdigestStatGroup();
    stat.tdigest = stat.getData();
    assertTrue(gson.toJson(stat).contains("groups"));
   }

   @Test
   public void testTdigestStatGroupAdd()
   {
    TdigestStatGroup stat = new TdigestStatGroup();
    Map<String, Long> group = new HashMap<>();
    group.put("redis", (long)24);
    group.put("sql",(long)4 );
    stat.addGroups(10000, group);
    assertTrue(stat.groups.keySet().contains("redis") && stat.groups.keySet().contains("sql"));
   }
}
