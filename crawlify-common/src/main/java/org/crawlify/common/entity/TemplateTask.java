package org.crawlify.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("template_task")
public class TemplateTask implements Serializable {

    /**
     * 任务ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String taskId;

    /**
     * 配置ID
     */
    private String configId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 父任务ID
     */
    private Integer pid;

    /**
     * 1 运行中
     * 2 停止
     * 3 运行完成
     * 4 运行失败
     */
    private Integer status;
}
