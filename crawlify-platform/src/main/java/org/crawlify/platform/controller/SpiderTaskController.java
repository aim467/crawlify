package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.entity.SpiderTask;
import org.crawlify.common.service.SpiderTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spiderTask")
public class SpiderTaskController {

    @Autowired
    private SpiderTaskService spiderTaskService;

    @PostMapping("/run")
    public R startTask(@RequestBody SpiderTask spiderTask) {
        return spiderTaskService.submitTask(spiderTask);
    }

    @PostMapping
    public boolean save(@RequestBody SpiderTask spiderTask) {
        return spiderTaskService.save(spiderTask);
    }

    @PutMapping
    public boolean update(@RequestBody SpiderTask spiderTask) {
        return spiderTaskService.updateById(spiderTask);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return spiderTaskService.removeById(id);
    }

    @GetMapping("/{id}")
    public SpiderTask getById(@PathVariable Long id) {
        return spiderTaskService.getById(id);
    }

    @GetMapping("/list")
    public Page<SpiderTask> list(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return spiderTaskService.page(new Page<>(page, size));
    }

    // 停止爬虫
    @GetMapping("/stop")
    public R stopSpiderTask(String taskId) {
        return spiderTaskService.stopSpiderTask(taskId);
    }

    @GetMapping("/async")
    public R asyncTaskStatus(String taskId) {
        return spiderTaskService.asyncTaskStatus(taskId);
    }
}