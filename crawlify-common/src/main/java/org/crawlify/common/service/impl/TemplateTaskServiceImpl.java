package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.poi.ss.formula.functions.T;
import org.crawlify.common.entity.TemplateConfig;
import org.crawlify.common.entity.TemplateTask;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.mapper.TemplateTaskMapper;
import org.crawlify.common.service.TemplateConfigService;
import org.crawlify.common.service.TemplateTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TemplateTaskServiceImpl extends ServiceImpl<TemplateTaskMapper, TemplateTask> implements TemplateTaskService {

    @Autowired
    private TemplateConfigService templateConfigService;

    @Override
    public R<Boolean> start(TemplateTask templateTask) {
        TemplateConfig templateConfig = templateConfigService.getById(templateTask.getConfigId());
        if (templateConfig.getUseScript()) {
            // 执行脚本
        }
        // 直接使用通用采集
        return null;
    }

    @Override
    public R stop(TemplateTask templateTask) {
        TemplateTask dbTask = getById(templateTask.getTaskId());
        // 如果任务在运行，并且有 PID，那么是特殊任务
        if (dbTask.getStatus() == 1 && dbTask.getPid() != null) {
            // 根据平台判断，使用 taskkill 或者是 kill 停止任务
            return null;
        }
        // 停止线程....
        return null;
    }
}