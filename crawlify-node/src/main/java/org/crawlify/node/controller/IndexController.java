package org.crawlify.node.controller;

import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.netty.PlatformServerHandler;
import org.crawlify.common.service.TaskNodeService;
import org.crawlify.common.service.WebsiteInfoService;
import org.crawlify.node.NodeEndListener;
import org.crawlify.node.cache.NodeCache;
import org.crawlify.node.config.RedisScheduler;
import org.crawlify.node.netty.SpiderNodeHolder;
import org.crawlify.node.processor.LinkProcessor;
import org.crawlify.node.pipeline.DatabasePipeline;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/")
@Slf4j
public class IndexController {

    @Resource
    private WebsiteInfoService websiteInfoService;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource // 确保使用 @Autowired 或 @Resource 注解
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${crawlify.master}")
    private String master;

    @Resource
    private TaskNodeService taskNodeService;

    @Value("${temp-authorization-key}")
    private String tempAuthorizationKey;


    @Deprecated
    @PostMapping("/run")
    public R runSpiderTask(@RequestBody TaskNode taskNode) {
        threadPoolTaskExecutor.execute(() -> {
            WebsiteInfo websiteInfo = websiteInfoService.getById(taskNode.getWebsiteId());
            int bitSize = 1 << 24; // 默认 16MB
            int[] seeds = new int[]{7, 11, 13, 31, 37, 61}; // 默认 6 个哈希函数
            Spider spider = Spider.create(new LinkProcessor(websiteInfo))
                    .setScheduler(new RedisScheduler(redisTemplate, stringRedisTemplate,
                            "bloom_" + taskNode.getTaskId(),
                            "queue_" + taskNode.getTaskId(), bitSize, seeds))
                    .setPipelines(List.of(new DatabasePipeline()))
                    .addUrl(websiteInfo.getBaseUrl())
                    .thread(taskNode.getThreadNum());
            NodeCache.spiderTaskCache.put(taskNode.getTaskId(), spider);
            NodeEndListener nodeEndListener = new NodeEndListener(taskNode
            );
            spider.addSpiderEndListener(nodeEndListener);
            spider.runAsync();
        });
        return R.ok();
    }

    @Deprecated
    @GetMapping("/stop")
    public R stopSpiderTask(String taskId) {
        Spider spider = NodeCache.spiderTaskCache.get(taskId);
        if (spider != null) {
            spider.stop();
            log.info("爬虫已停止");
            // 删除缓存中的爬虫任务
            NodeCache.spiderTaskCache.remove(taskId);
            return R.ok();
        }
        return R.fail("未找到爬虫");
    }

    @Resource
    private SpiderNodeHolder spiderNodeHolder;

    @GetMapping("/status")
    public R<SpiderNode> getSpiderTaskStatus() {
        SpiderNode spiderNode = spiderNodeHolder.getSpiderNode();
        spiderNode.setTaskCount(NodeCache.spiderTaskCache.size());
        spiderNode.setTaskCount(spiderNode.getTaskCount() == null ? 0 : spiderNode.getTaskCount());
        return R.ok(spiderNode);
    }
}