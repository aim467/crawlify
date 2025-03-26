package org.crawlify.node.processor;

import lombok.extern.slf4j.Slf4j;
import org.crawlify.node.entity.SpiderTask;
import org.crawlify.node.entity.WebsiteInfo;
import org.crawlify.node.entity.WebsiteLink;
import org.crawlify.node.service.WebsiteLinkService;
import org.crawlify.node.util.LinkExtractor;
import org.crawlify.node.util.SpringContextUtil;
import org.springframework.util.CollectionUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class LinkProcessor implements PageProcessor {
    private final WebsiteInfo websiteInfo;
    private final SpiderTask spiderTask;
    private final Pattern domainPattern;
    private final WebsiteLinkService websiteLinkService;


    public LinkProcessor(WebsiteInfo websiteInfo, SpiderTask spiderTask) {
        this.websiteInfo = websiteInfo;
        this.spiderTask = spiderTask;
        this.domainPattern = Pattern.compile(Pattern.quote(websiteInfo.getDomain()));
        this.websiteLinkService = SpringContextUtil.getBean(WebsiteLinkService.class);
    }

    // 爬虫配置（如重试次数、超时时间等）
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

    @Override
    public void process(Page page) {
        Set<String> links;
        links = LinkExtractor.extractLinks(page.getRequest().getUrl());
        // 过滤并处理链接
        List<String> targetRequests = new ArrayList<>();
        List<String> externalLinks = new ArrayList<>();

        links.forEach(link -> {
            try {
                String domain = getDomain(link);
                if (domain != null && domain.equals(websiteInfo.getDomain())) {
                    targetRequests.add(link);
                } else {
                    externalLinks.add(link);
                }
            } catch (Exception e) {
                log.error("Invalid URL: {}", link, e); // 处理非法URL
            }
        });

        // 添加同域名请求
        page.addTargetRequests(targetRequests);

        // 保存所有链接到数据库
        saveLinks(targetRequests, externalLinks, websiteInfo.getId());
    }

    private String getDomain(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    private void saveLinks(List<String> internalUrls, List<String> externalUrls, Integer websiteId) {
        List<WebsiteLink> websiteLinks = new ArrayList<>();

        internalUrls.forEach(url -> addLinkToWebsiteLinks(websiteLinks, url, websiteId, 1));
        externalUrls.forEach(url -> addLinkToWebsiteLinks(websiteLinks, url, websiteId, 2));

        if (CollectionUtils.isEmpty(websiteLinks)) {
            return;
        }
        websiteLinkService.batchSaveWebsiteLink(websiteLinks);
    }

    private void addLinkToWebsiteLinks(List<WebsiteLink> websiteLinks, String url, Integer websiteId, int type) {
        WebsiteLink websiteLink = new WebsiteLink();
        LocalDateTime now = LocalDateTime.now();
        websiteLink.setUrl(url);
        websiteLink.setWebsiteId(websiteId);
        websiteLink.setType(type);
        websiteLink.setCreatedAt(now);
        websiteLink.setUpdatedAt(now);
        websiteLinks.add(websiteLink);
    }

    @Override
    public Site getSite() {
        return site;
    }
}