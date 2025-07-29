package org.crawlify.common.dynamic;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
public class CrawlConfig {
    private String configId;
    // 配置标识，用于唯一追踪某个爬虫配置

    private String columnName;
    // 列名，通常用于标识数据列或分类

    private String columnUrl;
    // 基础 URL，第一页或无分页时的请求地址（必填）s

    private String requestType;
    // 请求类型，支持 GET 或 POST（必填）

    private String requestBody;
    // POST 请求时的请求体模板，可包含占位符 <pageNum>（可选）

    private int pageStart;
    // 起始页码，通常是 0 或 1（必填）

    private int pageLen;
    // 最大页码或总页数，用于控制分页循环条件（必填）

    private String nextPage;
    // 下一页 URL 模板，包含占位符 <pageNum>，主要用于 GET 请求（可选）

    private String requestHead;
    // 请求头信息，JSON 格式字符串，例如 {"Content-Type":"application/json", "Authorization":"Bearer xxx"}（可选）

    // --- 内部处理用 ---
    private Map<String, String> parsedHeaders;
    // 解析后的请求头，用于内部逻辑处理

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 占位符常量
    public static final String PAGE_NUM_PLACEHOLDER = "<pageNum>";
    // 分页占位符，用于动态替换页码

}
