package org.crawlify.common.proxy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 代理源
 */
@Slf4j
public class ProxySource {


    private static final String SECRET_ID = "";
    private static final String SECRET_KEY = "";
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;

    /**
     * 获取快代理的代理列表
     * <p>
     * <p>
     * 流程
     * 判断 redis token 是否存在，如果存在，直接使用
     *
     * @return
     */
    public static List<ProxyInfo> fetchProxiesFromKuaiDaiLi() {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            String token = jedis.get("kuai_token");

            if (token == null) {
                // 获取新token
                Map<String, Object> tokenData = getNewToken();
                if (tokenData != null) {
                    token = (String) tokenData.get("secret_token");
                    int expire = (int) tokenData.get("expire");
                    jedis.setex("kuai_token", expire, token);
                } else {
                    return new ArrayList<>(); // 获取token失败
                }
            }

            // 请求代理池接口
            String proxyUrl = "https://dps.kdlapi.com/api/getdps?secret_id=" + SECRET_ID + "&num=50%format=json&signature=" + token;
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(proxyUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode data = root.get("data");
                JsonNode proxyList = data.get("proxy_list");
                // 提取 root 中的 count
                int count = root.get("orderLeftCount").asInt();
                if (count <= 0) {
                    return new ArrayList<>(); // 没有代理了
                }

                List<ProxyInfo> proxies = new ArrayList<>();
                if (proxyList != null && proxyList.isArray()) {
                    for (JsonNode proxyNode : proxyList) {
                        String[] parts = proxyNode.asText().split(":");
                        if (parts.length == 2) {
                            ProxyInfo proxy = new ProxyInfo(parts[0], Integer.parseInt(parts[1]));
                            proxies.add(proxy);
                        }
                    }
                }
                return proxies;
            }
        } catch (Exception e) {
            log.error("获取快代理代理列表失败: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private static Map<String, Object> getNewToken() {
        try {
            String authUrl = "https://auth.kdlapi.com/api/get_secret_token";
            String requestBody = "secret_id=" + SECRET_ID + "&secret_key=" + SECRET_KEY;

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                int code = root.get("code").asInt();

                if (code == 0) {
                    JsonNode data = root.get("data");
                    String token = data.get("secret_token").asText();
                    int expire = data.get("expire").asInt();

                    return Map.of(
                            "secret_token", token,
                            "expire", expire
                    );
                }
            }
        } catch (Exception e) {
            log.error("获取快代理token失败: {}", e.getMessage());
        }
        return null;
    }

}