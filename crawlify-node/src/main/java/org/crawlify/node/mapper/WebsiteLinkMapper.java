package org.crawlify.node.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.crawlify.node.entity.WebsiteLink;

import java.util.List;

@Mapper
public interface WebsiteLinkMapper {
    @Insert("INSERT INTO website_link (url, description) VALUES (#{url}, #{description})")
    void insert(WebsiteLink websiteLink);

    void batchInsertOrUpdate(List<WebsiteLink> websiteLinks);
}