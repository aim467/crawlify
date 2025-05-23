package org.crawlify.node;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.service.SpiderTaskService;
import org.crawlify.common.service.TaskNodeService;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.SpiderEndListener;

import java.time.LocalDateTime;

@Slf4j
public class NodeEndListener implements SpiderEndListener {

    private final Integer finish = 3;
    private final Integer stop = 4;
    private final Integer error = 5;

    private TaskNodeService taskNodeService;

    private TaskNode taskNode;

    private String masterUrl;

    private String tempAuthorizationKey;

    public NodeEndListener(TaskNodeService taskNodeService, TaskNode taskNode,
                           String masterUrl, String tempAuthorizationKey) {
        this.taskNodeService = taskNodeService;
        this.taskNode = taskNode;
        this.masterUrl = masterUrl;
        this.tempAuthorizationKey = tempAuthorizationKey;
    }

    @Override
    public void onSpiderCompleted(Spider spider) {
        log.info("爬虫任务: {} 完成", spider.getUUID());
        updateTaskNodeAndSendToMaster(finish);
    }

    @Override
    public void onSpiderForceStopped(Spider spider) {
        log.info("爬虫任务: {} 已被强制停止", spider.getUUID());
        updateTaskNodeAndSendToMaster(stop);
    }

    @Override
    public void onSpiderAborted(Spider spider, Throwable throwable) {
        log.info("爬虫任务: {} 已被中断", spider.getUUID());
        updateTaskNodeAndSendToMaster(error);

    }

    private void updateTaskNodeAndSendToMaster(Integer status) {
        taskNode.setStatus(status);
        taskNode.setUpdatedAt(LocalDateTime.now());
        taskNodeService.updateById(taskNode);
        HttpResponse response = HttpRequest.get(masterUrl + "spiderTask/async?taskId=" + taskNode.getTaskId())
                .header("X-Crawlify-Token", tempAuthorizationKey).execute();
        log.info("回调结果：{}", response.body());
    }
}
