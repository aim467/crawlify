package org.crawlify.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.crawlify.common.entity.SpiderTask;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface SpiderTaskMapper extends BaseMapper<SpiderTask> {
    // 可以在此处添加自定义的查询方法
}