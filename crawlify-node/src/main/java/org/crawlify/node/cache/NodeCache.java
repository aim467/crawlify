package org.crawlify.node.cache;

import org.crawlify.common.entity.TaskNode;
import us.codecraft.webmagic.Spider;

import java.util.concurrent.ConcurrentHashMap;

public class NodeCache {

    // concurrentMap 爬虫任务缓存
    public static final ConcurrentHashMap<String, Spider> spiderTaskCache = new ConcurrentHashMap<>();
}
