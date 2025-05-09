package org.crawlify.platform.controller;

import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.dto.insert.SubmitTask;
import org.crawlify.common.dto.query.SpiderTaskQuery;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.entity.SpiderTask;
import org.crawlify.common.service.SpiderTaskService;
import org.crawlify.common.vo.SpiderTaskVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/spiderTask")
public class SpiderTaskController {

    @Autowired
    private SpiderTaskService spiderTaskService;

    @PostMapping("/run")
    public R startTask(@RequestBody SubmitTask submitTask) {
        return spiderTaskService.submitTask(submitTask);
    }

    @DeleteMapping("/{taskId}")
    public R<Boolean> delete(@PathVariable String taskId) {
        SpiderTask byId = spiderTaskService.getById(taskId);
        if (byId.getStatus() == 2) {
            return R.fail("任务在结束之前不允许删除");
        }
        return spiderTaskService.removeById(taskId) ? R.ok() : R.fail();
    }

    @GetMapping("/{id}")
    public SpiderTask getById(@PathVariable Long id) {
        return spiderTaskService.getById(id);
    }

    @GetMapping("/list")
    public R<PageResult<SpiderTaskVo>> list(SpiderTaskQuery query) {
        return R.ok(spiderTaskService.listTask(query));
    }

    // 停止爬虫
    @GetMapping("/stop")
    public R stopSpiderTask(String taskId) {
        return spiderTaskService.stopSpiderTask(taskId);
    }

    @GetMapping("/async")
    public R asyncTaskStatus(String taskId) {
        log.info("asyncTaskStatus: {}", taskId);
        return spiderTaskService.asyncTaskStatus(taskId);
    }
}