package org.crawlify.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 网站信息实体类，用于存储网站的基本信息。
 */
@Data
@TableName(value = "website_info", autoResultMap = true)
public class WebsiteInfo {
    /**
     * 主键ID，自增。
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 网站名称。
     */
    private String name;

    /**
     * 网站的基础URL。
     */
    private String baseUrl;

    /**
     * 网站的域名。
     */
    private String domain;

    /**
     * 网站的字符集编码。
     */
    private String charset;

    /**
     * 请求头信息，JSON格式存储。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> headers;

    /**
     * Cookie信息，JSON格式存储。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> cookies;

    /**
     * 请求超时时间（毫秒）。
     */
    private Integer timeOut;

    /**
     * 请求失败后的重试次数。
     */
    private Integer retryTimes;

    /**
     * 循环请求失败后的重试次数。
     */
    private Integer cycleRetryTimes;

    /**
     * 记录创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 记录更新时间。
     */
    private LocalDateTime updatedAt;
}