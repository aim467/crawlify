package org.crawlify.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.common.entity.TemplateTask;
import org.crawlify.common.entity.result.R;

public interface TemplateTaskService extends IService<TemplateTask> {
    R<Boolean> start(TemplateTask templateTask);

    R<Boolean> stop(TemplateTask templateTask);
}