package org.crawlify.common.vo;

import lombok.Data;
import org.crawlify.common.entity.result.PageResult;

@Data
public class SpiderTaskListVo {

    private PageResult<SpiderTaskVo> pageResult;

    private TaskStatusCount taskStatusCount;
}
