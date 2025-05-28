package org.crawlify.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.common.dto.insert.SubmitTask;
import org.crawlify.common.dto.query.SpiderTaskQuery;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.entity.SpiderTask;
import org.crawlify.common.vo.SpiderTaskListVo;
import org.crawlify.common.vo.SpiderTaskVo;

import java.security.PublicKey;

public interface SpiderTaskService extends IService<SpiderTask> {
    // 可以在此处添加自定义的服务方法

    public R submitTask(SubmitTask submitTask);

    public R stopSpiderTask(String taskId);

    public R asyncTaskStatus(String taskId);

    SpiderTaskListVo listTask(SpiderTaskQuery query);
}