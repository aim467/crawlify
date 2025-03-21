package org.crawlify.node.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.crawlify.node.entity.WebsiteInfo;

@Mapper
public interface WebsiteInfoMapper extends BaseMapper<WebsiteInfo> {

}