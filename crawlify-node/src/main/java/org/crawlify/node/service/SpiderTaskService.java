package org.crawlify.node.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.node.entity.R;
import org.crawlify.node.entity.SpiderTask;

public interface SpiderTaskService extends IService<SpiderTask> {
    // 可以在此处添加自定义的服务方法

    public R submitTask(SpiderTask task);
}