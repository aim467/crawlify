package org.crawlify.node.controller;

import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.TaskNodeService;
import org.crawlify.common.service.WebsiteInfoService;
import org.crawlify.node.cache.NodeCache;
import org.crawlify.node.config.RedisScheduler;
import org.crawlify.node.processor.LinkProcessor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;

import javax.annotation.Resource;

@RestController
@RequestMapping("/")
public class IndexController {

    @Resource
    private TaskNodeService taskNodeService;
    @Resource
    private WebsiteInfoService websiteInfoService;

    @Resource
    private RedisTemplate redisTemplate;

    @GetMapping("/ping")
    public R ping() {
        return R.ok();
    }

    @PostMapping("/run")
    public R runSpiderTask(TaskNode taskNode) {
        taskNodeService.save(taskNode);
        WebsiteInfo websiteInfo = websiteInfoService.getById(taskNode.getWebsiteId());
        int bitSize = 1 << 24; // 默认 16MB
        int[] seeds = new int[]{7, 11, 13, 31, 37, 61}; // 默认 6 个哈希函数
        Spider spider = Spider.create(new LinkProcessor(websiteInfo, null))
                .setScheduler(new RedisScheduler(redisTemplate,
                        "bloom_" + taskNode.getTaskId(),
                        "queue_" + taskNode.getTaskId(),
                        bitSize,
                        seeds))
                .addUrl(websiteInfo.getBaseUrl())
                .thread(taskNode.getThreadNum());
        NodeCache.spiderTaskCache.put(taskNode.getNodeId(), spider);
        spider.runAsync();
        return R.ok();
    }

    @GetMapping("/stop")
    public R stopSpiderTask(String nodeId) {
        Spider spider = NodeCache.spiderTaskCache.get(nodeId);
        if (spider != null) {
            spider.stop();
        }
        return R.ok();
    }
}
