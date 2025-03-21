package org.crawlify.platform.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "website_info")
public class WebsiteInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String baseUrl;
    private String domain;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
