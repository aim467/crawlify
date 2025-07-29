package org.crawlify.common.dto.insert;

import lombok.Data;
import org.crawlify.common.entity.SpiderNode;

import java.util.List;

@Data
public class SubmitTask {

    private Integer websiteId;

    private List<SpiderNode> spiderNodes;
}
