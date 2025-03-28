package org.crawlify.node.util;


import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class LinkExtractor {
    private static final Pattern ABSOLUTE_URL_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+-.]*://.*");
    private static final Pattern PROTOCOL_RELATIVE_URL_PATTERN = Pattern.compile("^//.*");

    private static final String[] IGNORED_PROTOCOLS = new String[]{"javascript:", "mailto:", "tel:", "data:"}; // 添加需要忽略的协议

    /**
     * 提取指定 URL 页面上的所有链接。
     *
     * @param pageUrl 要提取链接的页面 URL
     * @return 包含所有提取到的完整链接的 Set 集合
     */
    public static Set<String> extractLinks(String pageUrl) {

        Set<String> allLinks = new HashSet<>();
        if (!getUrlType(pageUrl).equals("webpage")) {
            return allLinks; // 如果不是网页，直接返回空集合
        }
        try {
            Document doc = Jsoup.connect(pageUrl).get();
            URL baseUrl = new URL(pageUrl);

            // 提取 <a> 标签中的 href 属性
            Elements aTags = doc.select("a[href]");
            for (Element link : aTags) {
                String href = link.attr("href");
                String absoluteUrl = resolveUrl(baseUrl, href);
                if (absoluteUrl != null) {
                    allLinks.add(absoluteUrl);
                }
            }

            // 提取 <script> 标签中的 src 属性
            Elements scriptTags = doc.select("script[src]");
            for (Element script : scriptTags) {
                String src = script.attr("src");
                String absoluteUrl = resolveUrl(baseUrl, src);
                if (absoluteUrl != null) {
                    allLinks.add(absoluteUrl);
                }
            }

            // 提取 <link> 标签中的 href 属性
            Elements linkTags = doc.select("link[href]");
            for (Element link : linkTags) {
                String href = link.attr("href");
                String absoluteUrl = resolveUrl(baseUrl, href);
                if (absoluteUrl != null) {
                    allLinks.add(absoluteUrl);
                }
            }

            // 提取 <img> 标签中的 src 属性
            Elements imgTags = doc.select("img[src]");
            for (Element img : imgTags) {
                String src = img.attr("src");
                String absoluteUrl = resolveUrl(baseUrl, src);
                if (absoluteUrl != null) {
                    allLinks.add(absoluteUrl);
                }
            }

        } catch (Exception e) {
            log.error("解析 URL 失败: {}，错误信息: {}", pageUrl, e.getMessage());
        }
        return allLinks;
    }

    /**
     * 判断 URL 地址的类型。
     *
     * @param urlString 要判断类型的 URL 地址
     * @return URL 的类型，例如 "webpage", "css", "javascript", "image", "other", "unknown"
     */
    public static String getUrlType(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return "unknown";
        }

        String lowerCaseUrl = urlString.toLowerCase();

        // 基于 URL 扩展名进行初步判断
        if (lowerCaseUrl.endsWith(".html") || lowerCaseUrl.endsWith(".htm") || lowerCaseUrl.endsWith("/")) {
            return "webpage";
        } else if (lowerCaseUrl.endsWith(".css")) {
            return "css";
        } else if (lowerCaseUrl.endsWith(".js")) {
            return "javascript";
        } else if (lowerCaseUrl.endsWith(".jpg") || lowerCaseUrl.endsWith(".jpeg") ||
                lowerCaseUrl.endsWith(".png") || lowerCaseUrl.endsWith(".gif") ||
                lowerCaseUrl.endsWith(".svg") || lowerCaseUrl.endsWith(".ico") ||
                lowerCaseUrl.endsWith(".webp")) {
            return "image";
        }

        // 尝试通过 Content-Type 头进行判断 (发送 HEAD 请求)
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();

            String contentType = connection.getContentType();
            if (contentType != null) {
                contentType = contentType.toLowerCase();
                if (contentType.contains("text/html")) {
                    return "webpage";
                }
            }
            connection.disconnect();

        } catch (MalformedURLException e) {
            System.err.println("无效的 URL: " + urlString);
        } catch (IOException e) {
            // HEAD 请求失败，可能是网络问题或服务器不允许 HEAD 请求
            System.err.println("获取 URL 类型失败 (HEAD 请求): " + urlString + "，错误信息: " + e.getMessage());
        }

        return "other"; // 默认返回 "other" 如果无法确定类型
    }

    /**
     * 解析 URL，处理相对路径。
     *
     * @param baseUrl 当前页面的基础 URL
     * @param linkUrl 要解析的链接 URL
     * @return 解析后的完整 URL，如果解析失败则返回 null
     */
    private static String resolveUrl(URL baseUrl, String linkUrl) {
        String trimmedLinkUrl = linkUrl.trim();

        // 如果已经是绝对 URL，直接返回
        if (ABSOLUTE_URL_PATTERN.matcher(trimmedLinkUrl).matches()) {
            return trimmedLinkUrl;
        }

        // 忽略特定的协议
        for (String protocol : IGNORED_PROTOCOLS) {
            if (trimmedLinkUrl.startsWith(protocol)) {
                return null; // 直接返回 null，表示不需要处理
            }
        }

        // 如果是协议相对 URL (例如: //example.com/path)，则使用 baseUrl 的协议
        if (PROTOCOL_RELATIVE_URL_PATTERN.matcher(trimmedLinkUrl).matches()) {
            try {
                return baseUrl.getProtocol() + ":" + trimmedLinkUrl;
            } catch (Exception e) {
                log.error("解析协议相对 URL 失败: {}，错误信息：{}", trimmedLinkUrl, e.getMessage());
                return null;
            }
        }

        // 如果是其他相对路径，才进行相对路径解析
        try {
            return new URL(baseUrl, trimmedLinkUrl).toString();
        } catch (MalformedURLException e) {
            log.error("解析相对 URL 失败：{}, 错误信息：{}", trimmedLinkUrl, e.getMessage());
            return null;
        }
    }
}