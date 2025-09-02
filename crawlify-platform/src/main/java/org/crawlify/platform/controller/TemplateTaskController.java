package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.crawlify.common.dto.query.TemplateTaskQuery;
import org.crawlify.common.entity.TemplateTask;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.TemplateTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/template/task")
public class TemplateTaskController {

    @Autowired
    private TemplateTaskService templateTaskService;

    @PostMapping
    public R<Boolean> save(@RequestBody TemplateTask templateTask) {
        return R.ok(templateTaskService.save(templateTask));
    }

    @PutMapping
    public R<Boolean> update(@RequestBody TemplateTask templateTask) {
        templateTaskService.updateById(templateTask);
        return R.ok(true);
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        return R.ok(templateTaskService.removeById(id));
    }

    @GetMapping("/{id}")
    public R<TemplateTask> getById(@PathVariable String id) {
        return R.ok(templateTaskService.getById(id));
    }


    @PostMapping("/list")
    public R<Page<TemplateTask>> page(@RequestBody TemplateTaskQuery query) {
        Page<TemplateTask> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<TemplateTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TemplateTask::getStatus, query.getStatus());
        queryWrapper.like(StringUtils.hasText(query.getTaskId()), TemplateTask::getTaskId, query.getTaskId());
        queryWrapper.like(StringUtils.hasText(query.getTaskId()), TemplateTask::getConfigId, query.getTaskId());
        Page<TemplateTask> templateTaskPage = templateTaskService.page(page, queryWrapper);
        return R.ok(templateTaskPage);
    }

    // 启动任务
    @PostMapping("/start")
    public R<Boolean> start(@RequestBody TemplateTask templateTask) {
        return templateTaskService.start(templateTask);
    }

    // 停止任务
    @PostMapping("/stop")
    public R<Boolean> stop(@RequestBody TemplateTask templateTask) {
        return templateTaskService.stop(templateTask);
    }
}