package org.crawlify.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.platform.entity.PageResult;
import org.crawlify.platform.entity.WebsiteLink;
import org.crawlify.platform.entity.WebsiteLinkQuery;

public interface WebsiteLinkService extends IService<WebsiteLink> {
    PageResult<WebsiteLink> queryLink(WebsiteLinkQuery query);
}