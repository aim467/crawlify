package org.crawlify.node.config;

import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.component.DuplicateRemover;

public class RedisBloomFilterDuplicateRemover implements DuplicateRemover {
    private final RedisBloomFilter bloomFilter;

    public RedisBloomFilterDuplicateRemover(RedisBloomFilter bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    @Override
    public boolean isDuplicate(Request request, Task task) {
        String url = request.getUrl();
        if (bloomFilter.mightContain(url)) {
            System.out.println("has visited: " + url);
            return true; // 已经访问过
        }
        bloomFilter.add(url); // 标记为已访问
        return false;
    }

    @Override
    public void resetDuplicateCheck(Task task) {
        // 清空布隆过滤器（可选）
    }

    @Override
    public int getTotalRequestsCount(Task task) {
        // 返回总请求数量（可选）
        return 0;
    }
}
