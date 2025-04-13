package org.crawlify.node.config;

import redis.clients.jedis.Jedis;


import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis 的简单布隆过滤器实现
 */
public class RedisBloomFilter {

    private RedisTemplate<String, Object> redisTemplate;
    // 在 Redis 中保存布隆过滤器的 key
    private String redisKey;
    // 位数组大小（比如 2^24）
    private int bitSize;
    // 哈希函数种子数组
    private int[] seeds;

    public RedisBloomFilter(RedisTemplate<String, Object> redisTemplate, String redisKey, int bitSize, int[] seeds) {
        this.redisTemplate = redisTemplate;
        this.redisKey = redisKey;
        this.bitSize = bitSize;
        this.seeds = seeds;
    }

    /**
     * 计算字符串的哈希值，使用不同种子实现多个哈希函数
     */
    private int hash(String value, int seed) {
        int result = 0;
        for (int i = 0; i < value.length(); i++) {
            result = seed * result + value.charAt(i);
        }
        // 限制在位数组范围内
        return (bitSize - 1) & result;
    }

    /**
     * 判断一个元素是否存在
     *
     * @param value 待检查的字符串（例如 URL）
     * @return true 表示可能已存在，false 表示一定不存在
     */
    public boolean contains(String value) {
        for (int seed : seeds) {
            int offset = hash(value, seed);
            Boolean bit = redisTemplate.opsForValue().getBit(redisKey, offset);
            if (bit == null || !bit) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将元素添加到布隆过滤器中
     *
     * @param value 待添加的字符串（例如 URL）
     */
    public void add(String value) {
        for (int seed : seeds) {
            int offset = hash(value, seed);
            redisTemplate.opsForValue().setBit(redisKey, offset, true);
        }
    }
}

