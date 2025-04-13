package org.crawlify.common.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.cache.PlatformCache;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.entity.SpiderTask;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.mapper.SpiderTaskMapper;
import org.crawlify.common.service.SpiderTaskService;
import org.crawlify.common.service.TaskNodeService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SpiderTaskServiceImpl extends ServiceImpl<SpiderTaskMapper, SpiderTask> implements SpiderTaskService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private TaskNodeService taskNodeService;

    @Override
    public R submitTask(SpiderTask task) {
        /**
         * 1 初始化
         * 2 运行
         * 3 完成
         * 4 停止
         */
        // 根据 websiteId 查询任务
        LambdaQueryWrapper<SpiderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpiderTask::getWebsiteId, task.getWebsiteId());
        wrapper.eq(SpiderTask::getStatus, Arrays.asList(2, 3));
        if (this.count(wrapper) > 0) {
            return R.fail("当前站点正在运行中，请勿重复提交");
        }
        String taskId = UUID.randomUUID().toString();
        task.setTaskId(UUID.randomUUID().toString());
        task.setStatus(2);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setWebsiteId(task.getWebsiteId());
        this.save(task);
        // 分发任务
        for (SpiderNode spiderNode : PlatformCache.spiderNodeCache.values()) {
            TaskNode taskNode = new TaskNode();
            taskNode.setNodeId(UUID.randomUUID().toString());
            taskNode.setNodeUrl("http://" + spiderNode.getNodeIp() + ":" + spiderNode.getNodePort() + "/");
            taskNode.setStatus(2);
            taskNode.setCreatedAt(LocalDateTime.now());
            taskNode.setUpdatedAt(LocalDateTime.now());
            taskNode.setTaskId(taskId);
            taskNode.setThreadNum(3);
            taskNode.setWebsiteId(task.getWebsiteId());
            taskNodeService.save(taskNode);
            HttpUtil.post(taskNode.getNodeUrl() + "run", JSON.toJSONString(taskNode));
        }
        return R.ok();
    }

    @Override
    public R stopSpiderTask(String taskId) {
        SpiderTask task = this.getById(taskId);
        if (task == null) {
            return R.fail("找不到任务");
        }
        // 找到 taskNode
        LambdaQueryWrapper<TaskNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskNode::getTaskId, taskId);
        List<TaskNode> list = taskNodeService.list(wrapper);
        for (TaskNode taskNode : list) {
            HttpUtil.post(taskNode.getNodeUrl() + "stop", JSON.toJSONString(taskNode));
            taskNode.setStatus(4);
        }
        taskNodeService.updateBatchById(list);
        task.setStatus(4);
        updateById(task);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public R asyncTaskStatus(String taskId) {
        LambdaQueryWrapper<TaskNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskNode::getTaskId, taskId);
        List<TaskNode> nodes = taskNodeService.list(wrapper);
        if (nodes.isEmpty()) return R.ok();

        boolean hasRunning = nodes.stream().anyMatch(n -> n.getStatus() == 2);
        boolean hasInit = nodes.stream().anyMatch(n -> n.getStatus() == 1);
        boolean allFinished = nodes.stream().allMatch(n -> n.getStatus() == 4);
        boolean allStopped = nodes.stream().allMatch(n -> n.getStatus() == 3);

        int newStatus;

        if (allFinished) {
            newStatus = 4; // 完成
        } else if (hasRunning) {
            newStatus = 2; // 运行
        } else if (hasInit) {
            newStatus = 1; // 初始化
        } else if (allStopped) {
            newStatus = 3; // 停止
        } else {
            // 部分完成、部分停止等情况
            newStatus = 5;
        }

        SpiderTask task = getById(taskId);
        if (task != null && !Objects.equals(task.getStatus(), newStatus)) {
            task.setStatus(newStatus);
            task.setUpdatedAt(LocalDateTime.now());
            updateById(task);
        }
        return R.ok();
    }
}