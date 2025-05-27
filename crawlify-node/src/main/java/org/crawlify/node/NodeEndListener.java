package org.crawlify.node;

import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.service.TaskNodeService;
import org.crawlify.node.cache.NodeCache;
import org.crawlify.node.netty.NodeClientHandler;
import org.crawlify.common.utils.SpringContextUtil;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderEndListener;

import java.time.LocalDateTime;

/**
 * NodeEndListener 是 WebMagic 爬虫框架的结束监听器实现类。
 * 它负责在爬虫任务完成、强制停止或中断时，更新任务状态并通知主节点。
 */
@Slf4j
public class NodeEndListener implements SpiderEndListener {

    /**
     * 定义任务状态常量
     */
    private final Integer finish = 3; // 完成
    private final Integer stop = 4;   // 停止
    private final Integer error = 5;  // 错误

    /**
     * 当前任务节点对象
     */
    private TaskNode taskNode;

    /**
     * 构造函数，初始化任务节点
     * @param taskNode 当前任务节点
     */
    public NodeEndListener(TaskNode taskNode) {
        this.taskNode = taskNode;
    }

    /**
     * 当爬虫任务完成时触发
     * @param spider 当前爬虫实例
     */
    @Override
    public void onSpiderCompleted(Spider spider) {
        log.info("爬虫任务: {} 完成", spider.getUUID());
        updateTaskNodeAndSendToMaster(finish);
    }

    /**
     * 当爬虫任务被强制停止时触发
     * @param spider 当前爬虫实例
     */
    @Override
    public void onSpiderForceStopped(Spider spider) {
        log.info("爬虫任务: {} 已被强制停止", spider.getUUID());
        updateTaskNodeAndSendToMaster(stop);
    }

    /**
     * 当爬虫任务被中断时触发
     * @param spider 当前爬虫实例
     * @param throwable 中断异常
     */
    @Override
    public void onSpiderAborted(Spider spider, Throwable throwable) {
        log.info("爬虫任务: {} 已被中断", spider.getUUID());
        updateTaskNodeAndSendToMaster(error);
    }

    /**
     * 更新任务节点状态并通知主节点
     * @param status 任务状态
     */
    private void updateTaskNodeAndSendToMaster(Integer status) {
        // 获取 TaskNodeService 实例
        TaskNodeService taskNodeService = SpringContextUtil.getBean(TaskNodeService.class);
        // 更新任务节点状态和时间戳
        taskNode.setStatus(status);
        taskNode.setUpdatedAt(LocalDateTime.now());
        // 保存任务节点状态
        taskNodeService.updateById(taskNode);
        try {
            // 获取 NodeClientHandler 实例
            NodeClientHandler nodeClientHandler = SpringContextUtil.getBean(NodeClientHandler.class);
            // 异步通知主节点任务完成
            nodeClientHandler.handleAsyncTask(taskNode.getTaskId());
            log.info("通过Netty同步任务状态: {}", taskNode.getTaskId());
            // 从缓存中移除已完成的任务
            NodeCache.spiderTaskCache.remove(taskNode.getTaskId());
        } catch (Exception e) {
            log.error("通过Netty同步任务状态失败", e);
        }
    }
}