package org.crawlify.node.util;

import org.crawlify.node.config.RedisBloomFilter;
import org.springframework.data.redis.core.RedisTemplate;

public class BloomFilterUtils {

    public static RedisBloomFilter createDefault(RedisTemplate<String, Object> redisTemplate, String redisKey) {
        int bitSize = 1 << 24; // 默认 16MB
        int[] seeds = new int[]{7, 11, 13, 31, 37, 61}; // 默认 6 个哈希函数
        return new RedisBloomFilter(redisTemplate, redisKey, bitSize, seeds);
    }
}