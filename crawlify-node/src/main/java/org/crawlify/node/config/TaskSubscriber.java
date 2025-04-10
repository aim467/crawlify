package org.crawlify.node.config;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.SpiderTask;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.node.processor.LinkProcessor;
import org.crawlify.common.service.WebsiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class TaskSubscriber implements MessageListener {

    @Autowired
    private WebsiteInfoService websiteInfoService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 反序列化消息内容
        String json = new String(message.getBody(), StandardCharsets.UTF_8);
        SpiderTask task = JSON.parseObject(json, SpiderTask.class);
        log.info("task: {}", task);
        WebsiteInfo websiteInfo = websiteInfoService.getById(task.getWebsiteId());

    }
}