package org.crawlify.common.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 网站的动态采集配置
 */
@TableName("dynamic_config")
@Data
public class DynamicConfig implements Serializable {

    // 网站ID
    private Integer websiteId;

    @TableId(type = IdType.ASSIGN_UUID)
    // 配置标识，用于唯一追踪某个爬虫配置
    private String configId;

    // 列名，通常用于标识数据列或分类
    private String configName;

    // 基础 URL，第一页或无分页时的请求地址（必填）
    private String columnUrl;

    // 请求类型，支持 GET 或 POST（必填）
    private String requestType;

    // POST 请求时的请求体模板，可包含占位符 <pageNum>（可选）
    private String requestBody;

    // 起始页码，通常是 0 或 1（必填）
    private int pageStart;

    // 最大页码或总页数，用于控制分页循环条件（必填）
    private int pageLen;

    // 下一页 URL 模板，包含占位符 <pageNum>，主要用于 GET 请求（可选）
    private String nextPage;

    // 请求头信息，JSON 格式字符串，例如 {"Content-Type":"application/json", "Authorization":"Bearer xxx"}（可选）
    private String requestHead;


    // --- 内部处理用 ---
    // 解析后的请求头，用于内部逻辑处理
    @TableField(exist = false)
    private Map<String, String> parsedHeaders;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    // 占位符常量
    @TableField(exist = false)
    public static final String PAGE_NUM_PLACEHOLDER = "<pageNum>";
    // 分页占位符，用于动态替换页码

    // 结果类型 json/xml
    private String resultType;

    // 结果清洗正则表达式
    private String resultClean;

    // 列表获取表达式
    private String resultListRule;

    // 提取详情页链接规则
    private String detailUrlRule;
}
