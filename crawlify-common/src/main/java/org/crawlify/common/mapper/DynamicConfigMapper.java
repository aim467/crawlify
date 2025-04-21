package org.crawlify.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.crawlify.common.entity.DynamicConfig;

@Mapper
public interface DynamicConfigMapper extends BaseMapper<DynamicConfig> {
}