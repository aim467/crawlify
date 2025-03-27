package org.crawlify.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.crawlify.common.entity.WebsiteLink;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WebsiteLinkMapper extends BaseMapper<WebsiteLink> {

    void batchInsertOrUpdate(List<WebsiteLink> websiteLinks);
}