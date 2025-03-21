package org.crawlify.node;

import org.crawlify.node.config.FilePipeline;
import org.crawlify.node.config.RedisBloomFilter;
import org.crawlify.node.config.RedisBloomFilterDuplicateRemover;
import org.crawlify.node.processor.LinkProcessor;
import redis.clients.jedis.Jedis;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.scheduler.QueueScheduler;

public class BloomFilterTest {
    public static void main(String[] args) {
        String url = "https://nexmoe.com/";

        // 创建 Redis 布隆过滤器
        RedisBloomFilter bloomFilter = new RedisBloomFilter("crawler:bloomfilter-" + url);

        // 创建自定义去重器
        RedisBloomFilterDuplicateRemover duplicateRemover = new RedisBloomFilterDuplicateRemover(bloomFilter);
        FilePipeline filePipeline = new FilePipeline("D:/crawled_urls.txt");
        // 启动爬虫
//        Spider.create(new LinkProcessor())
//                .addUrl(url)
//                .setScheduler(new QueueScheduler().setDuplicateRemover(duplicateRemover))
//                .addPipeline(filePipeline)
//                .thread(5)
//                .run();
    }
}
