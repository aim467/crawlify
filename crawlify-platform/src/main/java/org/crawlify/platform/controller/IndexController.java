package org.crawlify.platform.controller;

import cn.hutool.http.HttpRequest;
import org.crawlify.common.cache.PlatformCache;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.entity.result.R;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class IndexController {


    // 把 SpiderNode 保存到缓存中
    @PostMapping("/saveNode")
    public R saveNode(@RequestBody SpiderNode spiderNode) {
        spiderNode.setStatus(1);
        String uniqueIdentifier = spiderNode.getNodeIp() + ":" + spiderNode.getNodePort();
        String nodeId = UUID.nameUUIDFromBytes(uniqueIdentifier.getBytes()).toString();
        spiderNode.setNodeId(nodeId);
        PlatformCache.spiderNodeCache.putIfAbsent(nodeId, spiderNode);
        return R.ok();
    }


    @PostMapping("/refreshNode")
    public R<List<SpiderNode>> refreshNode() {
        // 获取所有节点，然后使用 http 请求获取判断节点是否返回200
        List<SpiderNode> spiderNodes = new ArrayList<>(PlatformCache.spiderNodeCache.values());
        for (SpiderNode spiderNode : spiderNodes) {
            String url = "http://" + spiderNode.getNodeIp() + ":" + spiderNode.getNodePort() + "/ping";
            // 请求url，获取 status_code，如果为200，则更新节点状态为1，否则更新节点状态为0
            int status = HttpRequest.get(url).execute().getStatus();
            if (status == 200) {
                spiderNode.setStatus(1);
            } else {
                spiderNode.setStatus(0);
            }
        }
        return R.ok(spiderNodes);
    }
}