package org.crawlify.node.processor;

import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.SpiderTask;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.entity.WebsiteLink;
import org.crawlify.common.service.WebsiteLinkService;
import org.crawlify.node.util.LinkUtils;
import org.crawlify.node.util.SpringContextUtil;
import org.springframework.util.CollectionUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class LinkProcessor implements PageProcessor {
    private final WebsiteInfo websiteInfo;
    private final SpiderTask spiderTask;
    private final WebsiteLinkService websiteLinkService;


    public LinkProcessor(WebsiteInfo websiteInfo, SpiderTask spiderTask) {
        this.websiteInfo = websiteInfo;
        this.spiderTask = spiderTask;
        this.websiteLinkService = SpringContextUtil.getBean(WebsiteLinkService.class);
    }

    // 爬虫配置（如重试次数、超时时间等）
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

    @Override
    public void process(Page page) {
        Set<String> links;
        links = LinkUtils.extractLinks(page.getRequest().getUrl());
        // 过滤并处理链接
        List<String> targetRequests = new ArrayList<>();
        List<String> externalLinks = new ArrayList<>();

        links.forEach(link -> {
            if (getDomain(link, websiteInfo.getDomain())) {
                targetRequests.add(link);
            } else {
                externalLinks.add(link);
            }
        });

        // 添加同域名请求
        page.addTargetRequests(targetRequests);

        // 此页面也保存到数据库
        targetRequests.add(page.getUrl().toString());

        // 保存所有链接到数据库
        saveLinks(targetRequests, externalLinks, websiteInfo.getId());
    }

    private boolean getDomain(String url, String domain) {
        String regex = "^https?://" +
                "(?:[a-zA-Z0-9-]+\\.)*" + // 子域名部分（非捕获组）
                Pattern.quote(domain) +    // 转义目标域名中的特殊字符
                "(?::\\d+)?" +             // 可选的端口号
                "(?:/|$|\\?)";             // 路径开始或结束

        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                .matcher(url)
                .find();
    }

    private void saveLinks(List<String> internalUrls, List<String> externalUrls, Integer websiteId) {
        List<WebsiteLink> websiteLinks = new ArrayList<>();

        internalUrls.forEach(url -> addLinkToWebsiteLinks(websiteLinks, url, websiteId, false));
        externalUrls.forEach(url -> addLinkToWebsiteLinks(websiteLinks, url, websiteId, true));

        if (CollectionUtils.isEmpty(websiteLinks)) {
            return;
        }
        websiteLinkService.batchSaveWebsiteLink(websiteLinks);
    }

    private void addLinkToWebsiteLinks(List<WebsiteLink> websiteLinks, String url, Integer websiteId, Boolean type) {
        WebsiteLink websiteLink = new WebsiteLink();
        LocalDateTime now = LocalDateTime.now();
        websiteLink.setUrl(url);
        websiteLink.setWebsiteId(websiteId);
        websiteLink.setExtLink(type);
        websiteLink.setCreatedAt(now);
        websiteLink.setUrlType(LinkUtils.typeMapping.get(LinkUtils.getUrlType(url)));
        websiteLink.setUpdatedAt(now);
        websiteLinks.add(websiteLink);
    }

    @Override
    public Site getSite() {
        return site;
    }
}