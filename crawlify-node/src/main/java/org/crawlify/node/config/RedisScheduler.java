package org.crawlify.node.config;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.scheduler.Scheduler;

import java.util.concurrent.TimeUnit;

public class RedisScheduler implements Scheduler {
    private RedisTemplate<String, Object> redisTemplate;
    private RedisBloomFilter bloomFilter;
    // 队列在 Redis 中的 key
    private String queueKey;

    /**
     * 构造方法
     *
     * @param redisTemplate 注入的 RedisTemplate
     * @param bloomKey      布隆过滤器在 Redis 中存储的 key
     * @param queueKey      队列在 Redis 中存储的 key
     * @param bitSize       布隆过滤器的位数组大小（如 2^24）
     * @param seeds         哈希函数的种子数组，如 {7, 11, 13, 31, 37, 61}
     */
    public RedisScheduler(RedisTemplate<String, Object> redisTemplate,
                          StringRedisTemplate stringRedisTemplate,
                          String bloomKey, String queueKey, int bitSize, int[] seeds) {
        this.redisTemplate = redisTemplate;
        this.bloomFilter = new RedisBloomFilter(stringRedisTemplate, bloomKey, bitSize, seeds);
        this.queueKey = queueKey;
    }

    /**
     * 将请求推入调度队列，同时利用布隆过滤器进行去重
     */
    @Override
    public void push(Request request, Task task) {
        String url = request.getUrl();
        // 如果布隆过滤器中不存在此 URL，则添加并放入队列
        if (!bloomFilter.contains(url)) {
            bloomFilter.add(url);
            redisTemplate.opsForList().leftPush(queueKey, request);
        }
    }

    /**
     * 从队列中弹出一个 Request。这里使用阻塞弹出方式（超时 5 秒）。
     */
    @Override
    public Request poll(Task task) {
        // 阻塞弹出
        Object obj = redisTemplate.opsForList().rightPop(queueKey, 5, TimeUnit.SECONDS);
        return obj instanceof Request ? (Request) obj : null;
    }
}
