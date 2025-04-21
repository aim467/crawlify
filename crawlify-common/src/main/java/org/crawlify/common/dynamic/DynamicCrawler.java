package org.crawlify.common.dynamic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*; // OkHttp 核心类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class DynamicCrawler {

    private static final Logger log = LoggerFactory.getLogger(DynamicCrawler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper(); // 用于解析 Header JSON
    private static final OkHttpClient client;

    static {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 连接超时
                .readTimeout(60, TimeUnit.SECONDS) // 读取超时
                .writeTimeout(60, TimeUnit.SECONDS) // 写入超时
                .followRedirects(true) // 允许重定向
                // 可以添加更多配置，如代理、拦截器等
                .build();
    }

    private final CrawlConfig config;

    public DynamicCrawler(CrawlConfig config) {
        Objects.requireNonNull(config, "CrawlConfig cannot be null");
        Objects.requireNonNull(config.getColumnUrl(), "columnUrl cannot be null");
        Objects.requireNonNull(config.getRequestType(), "requestType cannot be null");
        if (config.getPageStart() < 0) {
            throw new IllegalArgumentException("pageStart must be non-negative.");
        }
        this.config = config;
        parseHeaders(); // 初始化时解析 Header
    }

    /**
     * 解析配置中的 Header 字符串 (多行 Key: Value 格式)
     */
    private void parseHeaders() {
        // 先设置一个默认的空 Map，防止 NullPointerException
        config.setParsedHeaders(Collections.emptyMap());

        String rawHeaders = config.getRequestHead();

        if (rawHeaders == null || rawHeaders.trim().isEmpty()) {
            log.debug("Request head is empty for configId [{}]. No headers will be added.", config.getConfigId());
            return; // 没有 Header 需要解析
        }

        Map<String, String> headers = new HashMap<>();
        try {
            // 使用正则表达式 \\R 匹配各种换行符 (\n, \r, \r\n 等)
            String[] lines = rawHeaders.split("\\R");

            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) {
                    continue; // 跳过空行
                }

                int colonIndex = trimmedLine.indexOf(':');
                if (colonIndex > 0) { // 确保 key 不是空的，并且找到了冒号
                    String key = trimmedLine.substring(0, colonIndex).trim();
                    String value = trimmedLine.substring(colonIndex + 1).trim();

                    if (!key.isEmpty()) {
                        // 如果同一个 header key 出现多次，后面的会覆盖前面的
                        // OkHttp 的 addHeader 允许重复，但这里 Map 结构会覆盖
                        // 如果需要支持重复 Header，需要将 Map<String, String> 改为 Map<String, List<String>>
                        // 但对于大多数请求场景，覆盖是可接受的。
                        headers.put(key, value);
                        log.trace("Parsed header for configId [{}]: {} -> {}", config.getConfigId(), key, value);
                    } else {
                        log.warn("Skipping header line with empty key for configId [{}]: '{}'", config.getConfigId(),
                                trimmedLine);
                    }
                } else {
                    log.warn("Skipping malformed header line for configId [{}]: '{}'", config.getConfigId(),
                            trimmedLine);
                }
            }

            if (!headers.isEmpty()) {
                config.setParsedHeaders(headers);
                log.debug("Successfully parsed {} headers for configId [{}]", headers.size(), config.getConfigId());
            } else {
                log.debug("Request head string provided but no valid headers parsed for configId [{}].",
                        config.getConfigId());
            }

        } catch (Exception e) {
            // 捕获潜在的字符串处理异常
            log.error("Error parsing multi-line request head string for configId [{}]: {}", config.getConfigId(),
                    rawHeaders, e);
            // 出错时，保持 parsedHeaders 为空 Map
            config.setParsedHeaders(Collections.emptyMap());
        }
    }

    /**
     * 执行采集任务
     *
     * @return 返回每一页获取到的原始响应体字符串列表
     */
    public List<String> crawl() {
        List<String> results = new ArrayList<>();
        log.info("Starting crawl for configId [{}]", config.getConfigId());

        // 循环处理分页
        for (int currentPage = config.getPageStart(); currentPage <= config.getPageLen(); currentPage++) {
            log.debug("Crawling page {} for configId [{}]", currentPage, config.getConfigId());

            Request request = buildRequestForPage(currentPage);
            if (request == null) {
                log.error("Failed to build request for page {} and configId [{}]. Skipping.", currentPage,
                        config.getConfigId());
                continue; // 或者可以选择中断整个爬取
            }

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String responseBody = body.string(); // 注意：body().string() 只能调用一次
                        results.add(responseBody);
                        log.debug("Successfully fetched page {} for configId [{}]. Response size: {} bytes",
                                currentPage, config.getConfigId(), responseBody.length());
                        // 打印 responseBody
                        log.debug("Response body for page {}: {}", currentPage, responseBody);
                        // 在此可以添加对 responseBody 的初步检查，例如判断是否为空列表，提前终止分页
                        if (isEmptyResponse(responseBody)) {
                            log.info("Detected empty response on page {} for configId [{}]. Stopping pagination.",
                                    currentPage, config.getConfigId());
                            break; // 如果服务器返回空数据，可以提前结束
                        }
                    } else {
                        log.warn("Response body is null for page {} and configId [{}]", currentPage,
                                config.getConfigId());
                    }
                } else {
                    log.error("HTTP error for page {} and configId [{}]: Code={}, Message={}, URL={}",
                            currentPage, config.getConfigId(), response.code(), response.message(), request.url());
                    // 可以根据需要添加重试逻辑或中断爬取
                    // break; // 例如：如果某一页失败，则停止后续页面的抓取
                }
            } catch (IOException e) {
                log.error("IOException during request for page {} and configId [{}]: URL={}", currentPage,
                        config.getConfigId(), request.url(), e);
                // 可以根据需要添加重试逻辑或中断爬取
                // break; // 例如：网络错误时停止
            }

            // 添加请求间隔，避免过于频繁访问目标服务器
            try {
                TimeUnit.MILLISECONDS.sleep(500); // 休眠 500 毫秒，可以配置化
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Crawler sleep interrupted for configId [{}]", config.getConfigId());
                break;
            }
        }

        log.info("Finished crawl for configId [{}]. Total pages fetched: {}", config.getConfigId(), results.size());
        return results;
    }

    /**
     * 根据当前页码构建 OkHttp Request 对象
     *
     * @param pageNum 当前页码
     * @return Request 对象，如果构建失败则返回 null
     */
    private Request buildRequestForPage(int pageNum) {
        Request.Builder requestBuilder = new Request.Builder();
        // 这里会先对请求方法为 post 并且 有 nextPage 的进行占位符替换
        String targetUrl = determineUrl(pageNum);
        RequestBody requestBody = null;

        // 添加 Headers
        if (config.getParsedHeaders() != null) {
            config.getParsedHeaders().forEach(requestBuilder::addHeader);
        }

        // 处理 GET 请求
        if (config.getRequestType().equals("GET")) {
            requestBuilder.get();
        }
        // 处理 POST 请求
        else if (config.getRequestType().equals("POST")) {
            String bodyContent = "";
            if (config.getRequestBody() != null) {
                // 替换请求体中的占位符
                bodyContent = config.getRequestBody().replace(CrawlConfig.PAGE_NUM_PLACEHOLDER,
                        String.valueOf(pageNum));
            }
            // 猜测 Content-Type，或者让配置提供 Content-Type
            MediaType mediaType = determineMediaType(config.getParsedHeaders());
            requestBody = RequestBody.create(bodyContent, mediaType);
            requestBuilder.post(requestBody);
        } else {
            log.error("Unsupported request type: {} for configId [{}]", config.getRequestType(), config.getConfigId());
            return null; // 不支持的类型
        }

        try {
            requestBuilder.url(targetUrl);
            return requestBuilder.build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid URL constructed: {} for configId [{}]", targetUrl, config.getConfigId(), e);
            return null;
        }
    }

    /**
     * 决定当前页实际请求的 URL
     */
    private String determineUrl(int pageNum) {
        // 第一页 或 没有 nextPage 模板时，使用 columnUrl
        if (pageNum == config.getPageStart() || config.getNextPage() == null || config.getNextPage().trim().isEmpty()) {
            // 对于 POST，即使不是第一页，URL 通常也是固定的，变的是 Body。
            // 但如果 nextPage 模板存在且是 POST，优先使用 nextPage 模板替换 URL 中的页码 (虽然不常见)
            if (config.getRequestType().equals("POST") && config.getNextPage() != null
                    && !config.getNextPage().trim().isEmpty()) {
                return config.getNextPage().replace(CrawlConfig.PAGE_NUM_PLACEHOLDER, String.valueOf(pageNum));
            }
            // 默认情况：使用 columnUrl
            return config.getColumnUrl();
        }
        // 对于 GET 请求的后续页，使用 nextPage 模板
        else if (config.getRequestType().equals("GET")) {
            return config.getNextPage().replace(CrawlConfig.PAGE_NUM_PLACEHOLDER, String.valueOf(pageNum));
        }
        // 其他情况（理论上不应发生，但在 buildRequestForPage 中已处理 POST）
        else {
            return config.getColumnUrl(); // Fallback
        }
    }

    /**
     * 根据 Headers 推断或提供默认的 MediaType (用于 POST)
     */
    private MediaType determineMediaType(Map<String, String> headers) {
        String contentType = headers != null ? headers.getOrDefault("Content-Type", "application/json; charset=utf-8")
                : "application/json; charset=utf-8";
        // 尝试从 Content-Type header 解析 MediaType
        MediaType mediaType = MediaType.parse(contentType);
        // 如果解析失败，提供一个安全的默认值
        if (mediaType == null) {
            log.warn("Could not parse Content-Type '{}'. Defaulting to application/json; charset=utf-8", contentType);
            mediaType = MediaType.parse("application/json; charset=utf-8");
        }
        return mediaType;
    }

    /**
     * 判断响应体是否表示空数据列表（需要根据实际 API 返回情况调整）
     *
     * @param responseBody 响应体字符串
     * @return 如果是空数据，返回 true
     */
    private boolean isEmptyResponse(String responseBody) {
        if (responseBody == null)
            return true;
        String trimmedBody = responseBody.trim();
        // 简单判断：是否为空 JSON 数组 "[]" 或空字符串 ""
        // TODO: 需要根据实际 API 返回的空数据格式进行更精确的判断
        // 例如，可能返回 {"data": []} 或 XML 的空列表等
        return trimmedBody.isEmpty();
    }

    public static void main(String[] args) {
        CrawlConfig build = CrawlConfig.builder()
                .configId("1111")
                .columnUrl("http://localhost:8080/user/postUserList?page=1&size=10") // 第一页 URL
                .requestType("POST")
                .requestBody("{\"page\":1,\"size\":10}")
                .pageStart(1) // 页码从 1 开始
                .pageLen(100) // 抓取到第 10 页
                .nextPage("http://localhost:8080/user/postUserList?page=<pageNum>&size=10") // 后续页 URL 模板
                .build();
        DynamicCrawler crawler = new DynamicCrawler(build);
        crawler.crawl();

    }
}