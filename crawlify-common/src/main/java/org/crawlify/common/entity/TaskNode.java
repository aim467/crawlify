package org.crawlify.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("task_node")
public class TaskNode implements Serializable {

    /**
     * 节点id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String nodeId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 任务id spider_task
     */
    private String taskId;

    /**
     * 网站id website_info
     * @see org.crawlify.common.entity.WebsiteInfo
     */
    private Integer websiteId;

    /**
     * 线程数
     */
    private Integer threadNum;

    /**
     * 节点url
     */
    private String nodeUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
