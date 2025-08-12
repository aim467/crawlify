package org.crawlify.common.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.params.SetParams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 使用 Redis 的分布式代理池管理器
 * 注意：这个类本身不再是单例，因为每个爬虫实例都可以创建自己的管理器，
 * 但它们通过 Redis 连接到的是同一个共享的池。
 */
@Slf4j
public class RedisProxyPoolManager {

    // --- Redis Key 定义 ---
    private static final String KEY_RAW_PROXY_SET = "proxy:raw";
    private static final String KEY_VALID_PROXY_ZSET = "proxy:valid";
    private static final String KEY_REFILL_LOCK = "proxy:refill_lock";

    // --- 配置参数 ---
    private static final String VALIDATION_TARGET_URL = "http://httpbin.org/get";
    private static final int VALIDATION_TIMEOUT_MS = 5000;
    private static final int MIN_VALID_POOL_SIZE = 20; // 可用池最小保有量
    private static final long API_CALL_INTERVAL_MS = 1 * 60 * 1000; // API 调用间隔 (1分钟)
    private static final long LOCK_EXPIRATION_MS = 10 * 1000; // 分布式锁的过期时间 (10秒)

    private final JedisPool jedisPool;
    private final ExecutorService validatorExecutor;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public RedisProxyPoolManager(String redisHost, int redisPort) {
        // 配置 Jedis 连接池
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        this.jedisPool = new JedisPool(poolConfig, redisHost, redisPort);

        // 创建验证器线程池
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.validatorExecutor = Executors.newFixedThreadPool(coreCount);

        // 启动后台任务
        startBackgroundJobs();
    }

    /**
     * 从 Redis 可用池中获取一个代理
     *
     * @return 代理字符串 "host:port"，如果池为空则返回 null
     */
    public String getProxy() {
        try (Jedis jedis = jedisPool.getResource()) {
            // ZPOPMIN: 原子性地移除并返回分数最低的成员
            Set<Tuple> results = jedis.zpopmin(KEY_VALID_PROXY_ZSET, 1);
            if (results != null && !results.isEmpty()) {
                return results.iterator().next().getElement();
            }
        }
        return null;
    }

    /**
     * 启动定时任务：
     * 1. 定期检查是否需要补充IP
     * 2. 定期启动验证器
     * 3. 定期打印状态
     */
    private void startBackgroundJobs() {
        // 定期触发补充流程
        scheduler.scheduleAtFixedRate(this::checkAndRefill, 5, 10, TimeUnit.SECONDS);
        // 定期触发验证流程
        scheduler.scheduleAtFixedRate(this::triggerValidation, 1, 2, TimeUnit.SECONDS);
        // 定期打印状态
        scheduler.scheduleAtFixedRate(this::logPoolStatus, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * 检查并根据需要补充原始IP池
     */
    private void checkAndRefill() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 检查可用池大小
            if (jedis.zcard(KEY_VALID_PROXY_ZSET) < MIN_VALID_POOL_SIZE) {
                // 尝试获取分布式锁
                if (tryLock(jedis)) {
                    log.info("成功获取分布式锁，开始执行IP补充任务...");
                    try {
                        // 再次检查API调用频率 (虽然有锁，但最好还是检查一下)
                        String lastRefillTimeStr = jedis.get("proxy:last_refill_time");
                        long lastRefillTime = lastRefillTimeStr == null ? 0 : Long.parseLong(lastRefillTimeStr);
                        if (System.currentTimeMillis() - lastRefillTime < API_CALL_INTERVAL_MS) {
                            log.info("API调用频率过高，请稍后再试。");
                            return;
                        }

                        List<ProxyInfo> newProxies = ProxySource.fetchProxiesFromKuaiDaiLi();
                        if (!CollectionUtils.isEmpty(newProxies)) {
                            jedis.sadd(KEY_RAW_PROXY_SET, newProxies.toArray(new String[0]));
                            jedis.set("proxy:last_refill_time", String.valueOf(System.currentTimeMillis()));
                        }
                    } finally {
                        // 释放锁
                        unlock(jedis);
                        log.info("IP补充任务完成。");
                    }
                } else {
                    log.info("IP补充任务被其他实例占用，跳过。");
                }
            }
        }
    }

    /**
     * 触发验证任务
     */
    private void triggerValidation() {
        try (Jedis jedis = jedisPool.getResource()) {
            // 从原始池中随机弹出一个IP进行验证
            String proxyStr = jedis.spop(KEY_RAW_PROXY_SET);
            if (proxyStr != null) {
                validatorExecutor.submit(() -> validateProxy(proxyStr));
            }
        }
    }

    /**
     * 验证单个代理IP的可用性
     */
    private void validateProxy(String proxyStr) {
        String[] parts = proxyStr.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try {
            URL url = new URL(VALIDATION_TARGET_URL);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(VALIDATION_TIMEOUT_MS);
            connection.setReadTimeout(VALIDATION_TIMEOUT_MS);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                // 验证成功，加入可用池 (Sorted Set)
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.zadd(KEY_VALID_PROXY_ZSET, System.currentTimeMillis(), proxyStr);
                }
            }
        } catch (IOException e) {
            // 验证失败，什么也不做，IP被丢弃
        }
    }

    // --- 分布式锁实现 ---
    private boolean tryLock(Jedis jedis) {
        // SET key value NX PX milliseconds
        // NX: 只在key不存在时设置
        // PX: 设置过期时间（毫秒）
        String result = jedis.set(KEY_REFILL_LOCK, "locked", SetParams.setParams().nx().px(LOCK_EXPIRATION_MS));
        return "OK".equals(result);
    }

    private void unlock(Jedis jedis) {
        jedis.del(KEY_REFILL_LOCK);
    }

    // --- 监控和关闭 ---
    private void logPoolStatus() {
        try (Jedis jedis = jedisPool.getResource()) {
            long rawSize = jedis.scard(KEY_RAW_PROXY_SET);
            long validSize = jedis.zcard(KEY_VALID_PROXY_ZSET);
            log.info("[Redis状态监控] 原始池大小: {}, 可用池大小: {}", rawSize, validSize);
        }
    }

    public void shutdown() {
        log.info("正在关闭代理池管理器...");
        scheduler.shutdown();
        validatorExecutor.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS))
                scheduler.shutdownNow();
            if (!validatorExecutor.awaitTermination(5, TimeUnit.SECONDS))
                validatorExecutor.shutdownNow();
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            validatorExecutor.shutdownNow();
        }
        jedisPool.close();
        log.info("代理池管理器已关闭。");
    }


    /**
     * 主程序，演示如何使用
     */
    public static void main(String[] args) throws InterruptedException {
        // 假设 Redis 运行在 localhost:6379
        RedisProxyPoolManager manager = new RedisProxyPoolManager("localhost", 6379);

        // 模拟爬虫任务
        ExecutorService crawlerExecutor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            final int crawlerId = i + 1;
            crawlerExecutor.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    String proxy = manager.getProxy();
                    if (proxy != null) {
                        log.info("爬虫 [{}] 获取到代理: {}，开始执行任务...", crawlerId, proxy);
                    }
                    try {
                        Thread.sleep(ThreadLocalRandom.current().nextInt(800, 2000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        // 让演示运行2分钟
        log.info("--- 正在运行2分钟 ---");
        Thread.sleep(2 * 60 * 1000);

        log.info("--- 正在关闭系统 ---");
        crawlerExecutor.shutdownNow();
        manager.shutdown();
    }
}
