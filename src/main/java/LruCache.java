package io.airbrake.javabrake;

import java.util.Map;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
class LruCache<A, B> extends LinkedHashMap<A, B> {
  final int maxEntries;

  public LruCache(final int maxEntries) {
    super(maxEntries + 1, 1.0f, true);
    this.maxEntries = maxEntries;
  }

  @Override
  protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
    return super.size() > maxEntries;
  }
}
