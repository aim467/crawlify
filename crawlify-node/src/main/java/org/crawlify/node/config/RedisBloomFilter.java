package org.crawlify.node.config;

import redis.clients.jedis.Jedis;

public class RedisBloomFilter {
    private static final int BIT_SIZE = 2 << 28; // 布隆过滤器大小（约 512MB）
    private static final int HASH_FUNCTIONS = 6; // 哈希函数数量

    private final String key;

    public RedisBloomFilter(String key) {
        this.key = key;
    }

    /**
     * 添加元素到布隆过滤器
     */
    public void add(String value) {
        try (Jedis jedis = RedisPool.getJedis()) {
            for (int i = 0; i < HASH_FUNCTIONS; i++) {
                long hash = hash(value, i) % BIT_SIZE;
                jedis.setbit(key, hash, true);
            }
        }
    }

    public boolean mightContain(String value) {
        try (Jedis jedis = RedisPool.getJedis()) {
            for (int i = 0; i < HASH_FUNCTIONS; i++) {
                long hash = hash(value, i) % BIT_SIZE;
                if (!jedis.getbit(key, hash)) {
                    return false;
                }
            }
            return true;
        }
    }
    /**
     * 计算哈希值
     */
    private long hash(String value, int seed) {
        long hash = 0;
        for (char c : value.toCharArray()) {
            hash = hash * seed + c;
        }
        return Math.abs(hash);
    }
}
