package org.crawlify.common.protocol;

import lombok.Data;
import java.io.Serializable;

@Data
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageType type;
    private String nodeId;
    private Object data;

    public enum MessageType {
        REGISTER, // 节点注册
        HEARTBEAT, // 心跳
        TASK_ASSIGN, // 任务分配
        TASK_STATUS, // 任务状态更新
        NODE_STATUS, // 节点状态更新
        ASYNC_TASK, // 任务异步完成（新增）
        STOP // 任务停止（新增）
    }
}