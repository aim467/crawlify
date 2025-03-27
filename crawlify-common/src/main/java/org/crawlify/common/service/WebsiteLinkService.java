package org.crawlify.common.service;



import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.common.entity.WebsiteLink;
import org.crawlify.common.entity.query.WebsiteLinkQuery;
import org.crawlify.common.entity.result.PageResult;

import java.util.List;

public interface WebsiteLinkService extends IService<WebsiteLink> {
    PageResult<WebsiteLink> queryLink(WebsiteLinkQuery query);
    void batchSaveWebsiteLink(List<WebsiteLink> websiteLinks);
}