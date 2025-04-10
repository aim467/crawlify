package org.crawlify.platform.cache;

import org.crawlify.common.entity.SpiderNode;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformCache {

    public static final ConcurrentHashMap<String, SpiderNode> spiderNodeCache = new ConcurrentHashMap<>();

}
