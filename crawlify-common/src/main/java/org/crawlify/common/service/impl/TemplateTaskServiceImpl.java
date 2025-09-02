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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeroturnaround.exec.ProcessExecutor;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TemplateTaskServiceImpl extends ServiceImpl<TemplateTaskMapper, TemplateTask> implements TemplateTaskService {

    @Value("${crawlify..script-path-prefix}")
    private String scriptPathPrefix;


    @Autowired
    private TemplateConfigService templateConfigService;

    @Override
    public R<Boolean> start(TemplateTask templateTask) {
        TemplateConfig templateConfig = templateConfigService.getById(templateTask.getConfigId());
        if (templateConfig.getUseScript()) {
            // 执行脚本
            if (templateConfig.getScriptPath() == null) {
                return R.fail("开启脚本采集但是未配置脚本");
            }
            String taskId = UUID.randomUUID().toString();
            TemplateTask task = new TemplateTask();
            task.setConfigId(templateTask.getConfigId());
            task.setStatus(1);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
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