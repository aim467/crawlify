package org.crawlify.common.cache;

import io.netty.channel.Channel;
import org.crawlify.common.entity.SpiderNode;

import java.util.concurrent.ConcurrentHashMap;

public class PlatformCache {

    public static final ConcurrentHashMap<String, SpiderNode> spiderNodeCache = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, Channel> channelCache = new ConcurrentHashMap<>();

}
