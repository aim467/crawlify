package org.crawlify.node.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.crawlify.node.entity.SpiderTask;

@Mapper
public interface SpiderTaskMapper extends BaseMapper<SpiderTask> {
    // 可以在此处添加自定义的查询方法
}