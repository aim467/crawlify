package org.crawlify.common.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName(value = "template_config", autoResultMap = true)
public class TemplateConfig implements Serializable {

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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> requestHead;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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

    // 父页面链接
    private String parentLink;

    /**
     * 字段规则
     * 格式
     * [
     * {
     * "name": "字段名",
     * "rule": "提取规则",
     * "desc": "字段描述"
     * }
     * ]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, String>> fieldRules;

    /**
     * 是否使用脚本
     */
    private Boolean useScript;

    /**
     * 代码存放路径
     */
    private String scriptPath;

    @TableField(exist = false)
    private String scriptContent;
}
