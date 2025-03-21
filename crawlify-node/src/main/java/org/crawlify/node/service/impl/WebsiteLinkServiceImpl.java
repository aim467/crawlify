package org.crawlify.node.service.impl;

import org.crawlify.node.mapper.WebsiteLinkMapper;
import org.crawlify.node.entity.WebsiteLink;
import org.crawlify.node.service.WebsiteLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebsiteLinkServiceImpl implements WebsiteLinkService {

    @Autowired
    private WebsiteLinkMapper websiteLinkMapper;


    @Override
    public void batchSaveWebsiteLink(List<WebsiteLink> websiteLinks) {
        websiteLinkMapper.batchInsertOrUpdate(websiteLinks);
    }
}