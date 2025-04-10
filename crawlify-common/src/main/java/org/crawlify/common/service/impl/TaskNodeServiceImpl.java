package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.mapper.TaskNodeMapper;
import org.crawlify.common.service.TaskNodeService;
import org.springframework.stereotype.Service;

@Service
public class TaskNodeServiceImpl extends ServiceImpl<TaskNodeMapper, TaskNode> implements TaskNodeService {

}