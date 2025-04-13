package org.crawlify.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.entity.SpiderTask;

import java.security.PublicKey;

public interface SpiderTaskService extends IService<SpiderTask> {
    // 可以在此处添加自定义的服务方法

    public R submitTask(SpiderTask task);

    public R stopSpiderTask(String taskId);

    public R asyncTaskStatus(String taskId);
}