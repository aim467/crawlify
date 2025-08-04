package org.crawlify.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.common.entity.TaskNode;

public interface TaskNodeService extends IService<TaskNode> {
    void stopTaskNode(String taskNodeId);
}