package org.crawlify.node.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.WebsiteLink;
import org.crawlify.common.service.WebsiteLinkService;
import org.crawlify.node.util.LinkUtils;
import org.crawlify.common.utils.SpringContextUtil;
import org.springframework.util.CollectionUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DatabasePipeline implements Pipeline {
    @Override
    public void process(ResultItems resultItems, Task task) {
        WebsiteLinkService websiteLinkService = SpringContextUtil.getBean(WebsiteLinkService.class);
        Integer websiteId = resultItems.get("websiteId");
        String currentLink = resultItems.get("currentLink");
        LocalDateTime now = LocalDateTime.now();

        // 如果有currentLink的情况，那么当前页是非网页类型，直接保存
        if (currentLink != null) {
            WebsiteLink websiteLink = new WebsiteLink();
            websiteLink.setUrl(currentLink);
            websiteLink.setWebsiteId(websiteId);
            websiteLink.setExtLink(LinkUtils.getUrlType(currentLink).equals("webpage"));
            websiteLink.setCreatedAt(now);
            websiteLink.setUrlType(LinkUtils.typeMapping.get(LinkUtils.getUrlType(currentLink)));
            websiteLink.setUpdatedAt(now);
            websiteLinkService.batchSaveWebsiteLink(List.of(websiteLink));
            log.info("保存网页链接：{}", currentLink);
            return;
        }

        List<String> internalLinks = resultItems.get("internalLinks");
        List<String> externalLinks = resultItems.get("externalLinks");

        if ((internalLinks == null || internalLinks.isEmpty()) &&
                (externalLinks == null || externalLinks.isEmpty())) {
            return;
        }

        List<WebsiteLink> websiteLinks = new ArrayList<>();

        processLinks(internalLinks, websiteId, false, websiteLinks, now);
        processLinks(externalLinks, websiteId, true, websiteLinks, now);
        if (!CollectionUtils.isEmpty(websiteLinks)) {
            log.info("本次保存了{}条链接", websiteLinks.size());
            websiteLinkService.batchSaveWebsiteLink(websiteLinks);
        }

    }

    private void processLinks(List<String> links, Integer websiteId, Boolean isExternal,
                              List<WebsiteLink> websiteLinks, LocalDateTime now) {
        if (links == null) return;
        long startTime = System.currentTimeMillis();
        links.forEach(url -> {
            WebsiteLink websiteLink = new WebsiteLink();
            websiteLink.setUrl(url);
            websiteLink.setWebsiteId(websiteId);
            websiteLink.setExtLink(isExternal);
            websiteLink.setCreatedAt(now);
            websiteLink.setUrlType(LinkUtils.typeMapping.get(LinkUtils.getUrlType(url)));
            websiteLink.setUpdatedAt(now);
            websiteLinks.add(websiteLink);
        });
        log.info("本次处理了{}条链接，耗时{}ms", links.size(), System.currentTimeMillis() - startTime);
    }
}
