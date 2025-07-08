package org.crawlify.node.netty;

import org.crawlify.common.entity.SpiderNode;
import org.springframework.stereotype.Component;

@Component
public class SpiderNodeHolder {
    private volatile SpiderNode spiderNode;

    public SpiderNode getSpiderNode() {
        return spiderNode;
    }

    public void setSpiderNode(SpiderNode spiderNode) {
        this.spiderNode = spiderNode;
    }
}
