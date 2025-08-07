package org.crawlify.common.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(ProxyPoolManager.class);
    private static ProxyPoolManager instance;

    public static ProxyPoolManager getInstance() {
        if (instance == null) {
            synchronized (ProxyPoolManager.class) {
                if (instance == null) {
                    instance = new ProxyPoolManager();
                }
            }
        }
        return instance;
    }

    // --- 配置参数 ---
    private static final String VALIDATION_TARGET_URL = "http://httpbin.org/get"; // 用于测试代理有效性的稳定网址
    private static final int VALIDATION_TIMEOUT_MS = 5000; // 验证超时时间
    private static final int MIN_VALID_POOL_SIZE = 10; // 当可用池IP数低于此值，触发补充
    private static final long API_CALL_INTERVAL_MS = 1 * 60 * 1000; // API调用间隔

    // --- 核心数据结构 ---
    private final Queue<ProxyInfo> rawProxyQueue = new ConcurrentLinkedQueue<>();
    private final Queue<ProxyInfo> validProxyQueue = new ConcurrentLinkedQueue<>();

    // --- 异步组件 ---
    private final ExecutorService validatorExecutor; // 用于执行验证任务的线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(); // 用于定时任务
    private final AtomicBoolean isRefilling = new AtomicBoolean(false);
    private volatile long lastRefillTimestamp = 0;

    private ProxyPoolManager() {
        // 创建一个固定大小的线程池用于验证代理
        int coreCount = Runtime.getRuntime().availableProcessors();
        this.validatorExecutor = Executors.newFixedThreadPool(coreCount);

        // 启动一个定时任务，定期检查是否需要补充IP
        scheduler.scheduleAtFixedRate(this::checkAndRefill, 5, 30, TimeUnit.SECONDS);
        // 启动一个定时任务，定期打印池状态，方便监控
        scheduler.scheduleAtFixedRate(this::logPoolStatus, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * 从可用池中获取一个代理
     *
     * @return 一个确认可用的代理，如果池为空则返回null
     */
    public ProxyInfo getProxy() {
        return validProxyQueue.poll();
    }

    /**
     * [重要] 爬虫任务的反馈接口
     * 当一个代理在实际使用中失败时，调用此方法
     *
     * @param proxyInfo 失败的代理
     */
    public void reportFailure(ProxyInfo proxyInfo) {
        // 理论上，这个proxy已经被poll出去了，但这个方法可以用于记录失败日志或更复杂的失败策略
        logger.warn("反馈：代理 {} 在实际使用中失败，已被丢弃。", proxyInfo);
    }

    /**
     * 检查并补充原始IP池
     */
    private void checkAndRefill() {
        if (validProxyQueue.size() < MIN_VALID_POOL_SIZE && isRefilling.compareAndSet(false, true)) {
            logger.info("可用IP池数量低于阈值({}), 准备从API补充...", MIN_VALID_POOL_SIZE);

            // 异步执行，避免阻塞定时任务线程
            CompletableFuture.runAsync(() -> {
                long now = System.currentTimeMillis();
                if (now - lastRefillTimestamp < API_CALL_INTERVAL_MS) {
                    return; // API调用CD中
                }
                // TODO: 从代理提供者获取代理列表
                List<String> newProxies = mockFetchFromProxyProvider();
                if (CollectionUtils.isEmpty(newProxies)) {
                    return;
                }
                lastRefillTimestamp = System.currentTimeMillis();
                logger.info("成功从API获取 {} 个新IP，加入原始队列等待验证。", newProxies.size());
                try {
                    for (String proxyStr : newProxies) {
                        String[] parts = proxyStr.split(":");
                        ProxyInfo proxyInfo = new ProxyInfo(parts[0], Integer.parseInt(parts[1]));
                        if (rawProxyQueue.offer(proxyInfo)) {
                            // 每有一个新的未验证IP入队，就提交一个验证任务
                            validatorExecutor.submit(() -> validateProxy(proxyInfo));
                        }
                    }
                } finally {
                    isRefilling.set(false);
                }
            });
        }
    }

    /**
     * 验证单个代理IP的可用性
     *
     * @param proxyInfo 待验证的代理
     */
    private void validateProxy(ProxyInfo proxyInfo) {
        try {
            URL url = new URL(VALIDATION_TARGET_URL);
            Proxy proxy = proxyInfo.toJavaProxy();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
            connection.setConnectTimeout(VALIDATION_TIMEOUT_MS);
            connection.setReadTimeout(VALIDATION_TIMEOUT_MS);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            // 通常 200-299 范围的响应码表示成功
            if (responseCode >= 200 && responseCode < 300) {
                proxyInfo.setStatus(ProxyInfo.Status.VALID);
                validProxyQueue.offer(proxyInfo);
            } else {
                proxyInfo.setStatus(ProxyInfo.Status.INVALID);

            }
        } catch (IOException e) {
            proxyInfo.setStatus(ProxyInfo.Status.INVALID);
        }
    }

    private void logPoolStatus() {
        logger.info("[状态监控] 原始池大小: {}, 可用池大小: {}", rawProxyQueue.size(), validProxyQueue.size());
    }

    /**
     * 后续替换成实际的接口
     *
     * @return
     */
    private List<String> mockFetchFromProxyProvider() {
        // ... 模拟API调用 ...
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<String> proxies = new ArrayList<>();
        int baseIp = (int) (Math.random() * 200);
        for (int i = 0; i < 50; i++) { // 每次获取更多IP，因为不是所有IP都可用
            proxies.add("192.168." + baseIp + "." + (100 + i) + ":" + (8000 + i));
        }
        return proxies;
    }

    public void shutdown() {
        logger.info("正在关闭代理池管理器...");
        scheduler.shutdown();
        validatorExecutor.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!validatorExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                validatorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            validatorExecutor.shutdownNow();
        }
        logger.info("代理池管理器已关闭。");
    }
}
