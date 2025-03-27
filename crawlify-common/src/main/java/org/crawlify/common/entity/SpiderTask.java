package org.crawlify.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "spider_task")
public class SpiderTask {
    @TableId(type = IdType.ASSIGN_UUID)
    private String taskId;
    private Integer websiteId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
