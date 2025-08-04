package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.mapper.TaskNodeMapper;
import org.crawlify.common.service.SpiderTaskService;
import org.crawlify.common.service.TaskNodeService;
import org.crawlify.common.service.WebsiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskNodeServiceImpl extends ServiceImpl<TaskNodeMapper, TaskNode> implements TaskNodeService {

    @Lazy
    @Autowired
    private SpiderTaskService spiderTaskService;

    @Override
    public void stopTaskNode(String taskNodeId) {
        TaskNode byId = getById(taskNodeId);
        byId.setStatus(4);
        byId.setUpdatedAt(LocalDateTime.now());
        spiderTaskService.asyncTaskStatus(byId.getTaskId());
    }
}