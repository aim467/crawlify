package org.crawlify.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.crawlify.common.entity.TaskNode;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskNodeMapper extends BaseMapper<TaskNode> {
}