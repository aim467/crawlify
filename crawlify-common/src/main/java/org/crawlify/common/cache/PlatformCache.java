package org.crawlify.common.cache;

import io.netty.channel.Channel;
import org.crawlify.common.entity.SpiderNode;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlatformCache {

    public static final ConcurrentMap<String, SpiderNode> spiderNodeCache = new ConcurrentHashMap<>();

    public static final ConcurrentMap<String, Channel> channelCache = new ConcurrentHashMap<>();

}
