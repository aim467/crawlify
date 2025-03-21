package org.crawlify.node.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.node.entity.R;
import org.crawlify.node.entity.SpiderTask;
import org.crawlify.node.mapper.SpiderTaskMapper;
import org.crawlify.node.service.SpiderTaskService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.UUID;

@Service
public class SpiderTaskServiceImpl extends ServiceImpl<SpiderTaskMapper, SpiderTask> implements SpiderTaskService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public R submitTask(SpiderTask task) {
        /**
         * 1 初始化
         * 2 排队
         * 3 运行
         * 4 完成
         * 5 错误
         */
        // 根据 websiteId 查询任务
        LambdaQueryWrapper<SpiderTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SpiderTask::getWebsiteId, task.getWebsiteId());
        wrapper.eq(SpiderTask::getStatus, Arrays.asList(2, 3));
        if (this.count(wrapper) > 0) {
            return R.fail("当前站点正在运行中，请勿重复提交");
        }

        task.setTaskId(UUID.randomUUID().toString());
        task.setStatus(3);
        this.save(task);
        redisTemplate.convertAndSend("spiderTask", task);
        return R.ok("任务提交成功");
    }
}