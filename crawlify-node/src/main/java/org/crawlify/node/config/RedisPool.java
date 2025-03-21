package org.crawlify.node.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static final JedisPool jedisPool;

    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(128);          // 最大连接数
        config.setMaxIdle(32);            // 最大空闲连接数
        config.setMinIdle(8);             // 最小空闲连接数
        config.setTestOnBorrow(true);     // 获取连接时验证有效性
        config.setTestOnReturn(true);     // 归还连接时验证有效性
        config.setBlockWhenExhausted(true); // 连接耗尽时等待
        config.setMaxWaitMillis(5000);     // 获取连接最大等待时间

        jedisPool = new JedisPool(config, "192.168.1.188", 6379, 2000, null);
    }

    public static Jedis getJedis() {
        return jedisPool.getResource();
    }

    public static void close() {
        jedisPool.close();
    }
}
