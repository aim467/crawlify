package org.crawlify.node.controller;

import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.TaskNodeService;
import org.crawlify.common.service.WebsiteInfoService;
import org.crawlify.common.service.impl.TaskNodeServiceImpl;
import org.crawlify.node.processor.LinkProcessor;
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


    @GetMapping("/ping")
    public R ping() {
        return R.ok();
    }

    @PostMapping("/run")
    public R runSpiderTask(TaskNode taskNode) {
        taskNodeService.save(taskNode);
        WebsiteInfo websiteInfo = websiteInfoService.getById(taskNode.getWebsiteId());
        Spider.create(new LinkProcessor(websiteInfo, null))
                .addUrl(websiteInfo.getBaseUrl())
                .thread(taskNode.getThreadNum())
                .run();
        return R.ok();
    }
}
