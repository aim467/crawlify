package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.cache.PlatformCache;
import org.crawlify.common.dto.insert.SubmitTask;
import org.crawlify.common.dto.query.SpiderTaskQuery;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.entity.SpiderTask;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.WebsiteLink;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.mapper.SpiderTaskMapper;
import org.crawlify.common.service.SpiderTaskService;
import org.crawlify.common.service.TaskNodeService;
import org.crawlify.common.service.DynamicConfigService;
import org.crawlify.common.entity.DynamicConfig;
import org.crawlify.common.dynamic.DynamicCrawler;
import org.crawlify.common.service.WebsiteLinkService;
import org.crawlify.common.vo.SpiderTaskListVo;
import org.crawlify.common.vo.SpiderTaskVo;
import org.crawlify.common.vo.TaskStatusCount;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import io.netty.channel.Channel;
import org.crawlify.common.netty.PlatformServerHandler;
import org.crawlify.common.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SpiderTaskServiceImpl extends ServiceImpl<SpiderTaskMapper, SpiderTask> implements SpiderTaskService {

    private static final Logger log = LoggerFactory.getLogger(SpiderTaskServiceImpl.class);

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private TaskNodeService taskNodeService;

    @Resource
    private DynamicConfigService dynamicConfigService;

    @Resource
    private WebsiteLinkService websiteLinkService;

    @Override
    public R submitTask(SubmitTask submitTask) {
        /**
         * 1 初始化
         * 2 运行
         * 3 完成
         * 4 停止
         * 5 异常
         */
        // 根据 websiteId 查询任务
        LambdaQueryWrapper<SpiderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpiderTask::getWebsiteId, submitTask.getWebsiteId());
        wrapper.in(SpiderTask::getStatus, Arrays.asList(1, 2));
        if (count(wrapper) > 0) {
            return R.fail("当前站点正在运行中，请勿重复提交");
        }

        SpiderTask task = new SpiderTask();
        String taskId = UUID.randomUUID().toString();
        task.setTaskId(taskId);
        task.setStatus(2);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setWebsiteId(submitTask.getWebsiteId());
        this.save(task);

        // 检查网站是否有动态配置，如果有则先进行动态采集
        LambdaQueryWrapper<DynamicConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(DynamicConfig::getWebsiteId, submitTask.getWebsiteId());
        List<DynamicConfig> dynamicConfigs = dynamicConfigService.list(configWrapper);

        if (!dynamicConfigs.isEmpty()) {
            log.info("网站ID: {} 检测到 {} 个动态配置，开始执行动态采集", submitTask.getWebsiteId(), dynamicConfigs.size());
            // 执行动态配置采集
            for (DynamicConfig config : dynamicConfigs) {
                try {
                    log.info("开始执行动态配置采集，配置ID: {}, 配置名称: {}", config.getConfigId(), config.getConfigName());
                    DynamicCrawler dynamicCrawler = new DynamicCrawler(config);
                    List<String> crawlResults = dynamicCrawler.crawl();
                    log.info("动态配置采集完成，配置ID: {}, 采集到 {} 条数据", config.getConfigId(), crawlResults.size());
                    // 提取到的结果存入到数据库
                    List<WebsiteLink> collect = crawlResults.parallelStream().map(url -> {
                        WebsiteLink websiteLink = new WebsiteLink();
                        websiteLink.setUrl(url);
                        websiteLink.setExtLink(false);
                        websiteLink.setWebsiteId(submitTask.getWebsiteId());
                        websiteLink.setCreatedAt(LocalDateTime.now());
                        websiteLink.setUrlType(1);
                        websiteLink.setUpdatedAt(LocalDateTime.now());
                        return websiteLink;
                    }).collect(Collectors.toList());
                    websiteLinkService.batchSaveWebsiteLink(collect);

                } catch (Exception e) {
                    log.error("动态配置采集失败，配置ID: {}, 配置名称: {}, 错误信息: {}",
                            config.getConfigId(), config.getConfigName(), e.getMessage(), e);
                }
            }
        }

        // 使用Netty发送任务
        // 使用 submitTask 的 spiderNodes
        for (SpiderNode spiderNode : submitTask.getSpiderNodes()) {
            if (spiderNode.getStatus() == 0) {
                continue;
            }

            Channel channel = PlatformServerHandler.getNodeChannel(spiderNode.getNodeId());
            if (channel != null && channel.isActive()) {
                TaskNode taskNode = new TaskNode();
                taskNode.setNodeId(UUID.randomUUID().toString());
                taskNode.setNodeUrl("http://" + spiderNode.getNodeIp() + ":" + spiderNode.getNodePort() + "/");
                taskNode.setStatus(2);
                taskNode.setCreatedAt(LocalDateTime.now());
                taskNode.setUpdatedAt(LocalDateTime.now());
                taskNode.setTaskId(taskId);
                taskNode.setThreadNum(spiderNode.getThreadNum());
                taskNode.setWebsiteId(submitTask.getWebsiteId());
                taskNodeService.save(taskNode);

                Message message = new Message();
                message.setType(Message.MessageType.TASK_ASSIGN);
                message.setData(taskNode);
                channel.writeAndFlush(message);
            }
        }
        return R.ok();
    }

    @Override
    public R stopSpiderTask(String taskId) {
        SpiderTask task = this.getById(taskId);
        if (task == null) {
            return R.fail("找不到任务");
        }

        if (task.getStatus() == 3 || task.getStatus() == 4 || task.getStatus() == 5) {
            return R.fail("任务已完成无需停止");
        }
        // 找到 taskNode
        LambdaQueryWrapper<TaskNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskNode::getTaskId, taskId);
        List<TaskNode> list = taskNodeService.list(wrapper);
        for (TaskNode taskNode : list) {
            // 通过Netty向node发送STOP消息
            Channel channel = PlatformServerHandler.getNodeChannel(taskNode.getNodeId());
            if (channel != null && channel.isActive()) {
                Message message = new Message();
                message.setType(Message.MessageType.STOP);
                message.setNodeId(taskNode.getNodeId());
                message.setData(taskNode.getTaskId());
                channel.writeAndFlush(message);
            }
            taskNode.setStatus(4);
            task.setUpdatedAt(LocalDateTime.now());
            taskNodeService.updateById(taskNode);
        }
        task.setStatus(4);
        task.setUpdatedAt(LocalDateTime.now());
        updateById(task);
        return R.ok();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public R asyncTaskStatus(String taskId) {
        LambdaQueryWrapper<TaskNode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskNode::getTaskId, taskId);
        List<TaskNode> nodes = taskNodeService.list(wrapper);
        if (nodes.isEmpty())
            return R.ok();

        boolean hasRunning = nodes.stream().anyMatch(n -> n.getStatus() == 2);
        boolean hasInit = nodes.stream().anyMatch(n -> n.getStatus() == 1);
        boolean allFinished = nodes.stream().allMatch(n -> n.getStatus() == 3);
        boolean allStopped = nodes.stream().allMatch(n -> n.getStatus() == 4);
        boolean allError = nodes.stream().allMatch(n -> n.getStatus() == 5);

        int newStatus;

        if (allFinished) {
            newStatus = 3; // 完成
        } else if (hasRunning) {
            newStatus = 2; // 运行
        } else if (hasInit) {
            newStatus = 1; // 初始化
        } else if (allStopped) {
            newStatus = 4; // 停止
        } else if (allError) {
            newStatus = 5;
        } else {
            // 部分完成、部分停止等情况
            newStatus = 6;
        }

        SpiderTask task = getById(taskId);
        // 如果为以下状态则删除缓存
        if (newStatus == 3 || newStatus == 4 || newStatus == 5 || newStatus == 6) {
            redisTemplate.delete("queue_" + task.getTaskId());
            redisTemplate.delete("bloom_" + task.getTaskId());
        }

        if (task != null && !Objects.equals(task.getStatus(), newStatus)) {
            task.setStatus(newStatus);
            task.setUpdatedAt(LocalDateTime.now());
            updateById(task);
        }
        return R.ok();
    }

    @Override
    public SpiderTaskListVo listTask(SpiderTaskQuery query) {
        Page<SpiderTask> page = new Page<>(query.getPage(), query.getSize());
        IPage<SpiderTaskVo> taskIPage = this.baseMapper.listTask(page, query);
        PageResult<SpiderTaskVo> pageResult = new PageResult<>();
        pageResult.setCurrent(taskIPage.getCurrent());
        pageResult.setPages(taskIPage.getPages());
        pageResult.setSize(taskIPage.getSize());
        pageResult.setRecords(taskIPage.getRecords());
        pageResult.setTotal(taskIPage.getTotal());
        TaskStatusCount taskStatusCount = this.baseMapper.getTaskStatusCount();

        SpiderTaskListVo spiderTaskListVo = new SpiderTaskListVo();
        spiderTaskListVo.setPageResult(pageResult);
        spiderTaskListVo.setTaskStatusCount(taskStatusCount);
        return spiderTaskListVo;
    }
}