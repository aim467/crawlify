package org.crawlify.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.crawlify.common.dto.query.SpiderTaskQuery;
import org.crawlify.common.entity.SpiderTask;
import org.apache.ibatis.annotations.Mapper;
import org.crawlify.common.vo.SpiderTaskVo;

import java.util.List;

@Mapper
public interface SpiderTaskMapper extends BaseMapper<SpiderTask> {
    IPage<SpiderTaskVo> listTask(@Param("page") IPage<SpiderTask> page, @Param("query") SpiderTaskQuery query);
}