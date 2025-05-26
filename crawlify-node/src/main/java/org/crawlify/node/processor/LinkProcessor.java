package org.crawlify.node.processor;

import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.service.WebsiteLinkService;
import org.crawlify.node.util.LinkUtils;
import org.crawlify.common.utils.SpringContextUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
public class LinkProcessor implements PageProcessor {
    private final WebsiteInfo websiteInfo;

    private Site site;

    private final WebsiteLinkService websiteLinkService;


    public LinkProcessor(WebsiteInfo websiteInfo) {
        this.websiteInfo = websiteInfo;
        // 初始化 site 变量
        this.site = Site.me();
        site.setDomain(websiteInfo.getDomain());
        site.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/91.0.4472.124 Safari/537.36");

        if (StringUtils.hasText(websiteInfo.getCharset())) {
            site.setCharset(websiteInfo.getCharset());
        }
        if (Objects.nonNull(websiteInfo.getRetryTimes())) {
            site.setRetryTimes(websiteInfo.getRetryTimes());
        }
        if (Objects.nonNull(websiteInfo.getTimeOut())) {
            site.setTimeOut(websiteInfo.getTimeOut());
        }
        if (Objects.nonNull(websiteInfo.getCycleRetryTimes())) {
            site.setCycleRetryTimes(websiteInfo.getCycleRetryTimes());
        }
        if (!CollectionUtils.isEmpty(websiteInfo.getHeaders())) {
            websiteInfo.getHeaders().forEach((key, value) -> {
                site.addHeader(key, value.toString());
            });
        }
        if (!CollectionUtils.isEmpty(websiteInfo.getCookies())) {
            websiteInfo.getCookies().forEach((key, value) -> {
                site.addCookie(key, value.toString());
            });
        }

        this.websiteLinkService = SpringContextUtil.getBean(WebsiteLinkService.class);
    }


    @Override
    public void process(Page page) {
        // 如果不是网页类型，跳过搜索url部分
        page.getResultItems().put("websiteId", websiteInfo.getId());
        String currentUrl = page.getUrl().toString();
        if (!LinkUtils.getUrlType(currentUrl).equals("webpage")) {
            page.getResultItems().put("currentLink", currentUrl);
            return;
        }

        Set<String> links;
        links = LinkUtils.extractLinks(page.getRequest().getUrl());
        // 过滤并处理链接
        List<String> targetRequests = new ArrayList<>();
        List<String> externalLinks = new ArrayList<>();

        links.forEach(link -> {
            if (LinkUtils.getDomain(link, websiteInfo.getDomain())) {
                targetRequests.add(link);
            } else {
                externalLinks.add(link);
            }
        });

        // 添加同域名请求
        page.addTargetRequests(targetRequests);
        page.getResultItems().put("internalLinks", targetRequests);
        page.getResultItems().put("externalLinks", externalLinks);
    }

    @Override
    public Site getSite() {
        return site;
    }
}