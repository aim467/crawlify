package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import kotlin.jvm.internal.Lambda;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.TaskNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/taskNode")
public class TaskNodeController {

    @Autowired
    private TaskNodeService taskNodeService;

    /**
     * 根据主任务ID查询对于的节点任务列表
     * @param taskId
     * @return
     */
    @GetMapping("/list")
    public R<List<TaskNode>> list(String taskId) {
        LambdaQueryWrapper<TaskNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskNode::getTaskId, taskId);
        return R.ok(taskNodeService.list(wrapper));
    }

    @GetMapping("/stop")
    public R stopTaskNode(String taskNodeId) {
        taskNodeService.stopTaskNode(taskNodeId);
        return R.ok();
    }
}