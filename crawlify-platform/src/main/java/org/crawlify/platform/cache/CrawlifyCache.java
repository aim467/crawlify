package org.crawlify.platform.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CrawlifyCache {

    public static ConcurrentMap<String, Integer> cache = new ConcurrentHashMap<>();
}
