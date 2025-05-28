package org.crawlify.common.vo;

import lombok.Data;

/**
 * 任务状态统计类
 * 用于存储不同状态的任务数量
 */
@Data
public class TaskStatusCount {

    /**
     * 初始化状态的任务数量
     */
    private Integer initCount;

    /**
     * 运行中状态的任务数量
     */
    private Integer runningCount;

    /**
     * 完成状态的任务数量
     */
    private Integer completedCount;

    /**
     * 停止状态的任务数量
     */
    private Integer stoppedCount;

    /**
     * 失败状态的任务数量
     */
    private Integer failedCount;

    /**
     * 部分完成状态的任务数量
     */
    private Integer partialCount;
}