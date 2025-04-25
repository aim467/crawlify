package org.crawlify.node.config;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;


import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;

/**
 * 基于 Redis 的简单布隆过滤器实现
 */
public class RedisBloomFilter {

    private StringRedisTemplate stringRedisTemplate;
    // 在 Redis 中保存布隆过滤器的 key
    private String redisKey;
    // 位数组大小（比如 2^24）
    private int bitSize;
    // 哈希函数种子数组
    private int[] seeds;

    public RedisBloomFilter(StringRedisTemplate stringRedisTemplate, String redisKey, int bitSize, int[] seeds) {
        this.stringRedisTemplate = stringRedisTemplate;
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
            Boolean bit = stringRedisTemplate.opsForValue().getBit(redisKey, offset);
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
        byte[] keyBytes = redisKey.getBytes(StandardCharsets.UTF_8);
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (int seed : seeds) {
                long offset = hash(value, seed);
                // 直接在 RedisConnection 上 setBit
                connection.setBit(keyBytes, offset, true);
            }
            return null;
        });
    }
}

