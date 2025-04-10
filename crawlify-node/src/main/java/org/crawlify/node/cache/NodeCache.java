package org.crawlify.node.cache;

import org.crawlify.common.entity.TaskNode;

import java.util.concurrent.ConcurrentHashMap;

public class NodeCache {

    // concurrentMap 爬虫任务缓存
    private static final ConcurrentHashMap<String, TaskNode> spiderTaskCache = new ConcurrentHashMap<>();
}
