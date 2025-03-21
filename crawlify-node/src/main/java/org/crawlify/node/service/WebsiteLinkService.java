package org.crawlify.node.service;

import org.crawlify.node.entity.WebsiteLink;

import java.util.List;

public interface WebsiteLinkService {
    void batchSaveWebsiteLink(List<WebsiteLink> websiteLinks);
}