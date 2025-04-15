package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.WebsiteLink;
import org.crawlify.common.dto.query.WebsiteLinkQuery;
import org.crawlify.common.mapper.WebsiteLinkMapper;
import org.crawlify.common.service.WebsiteLinkService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
public class WebsiteLinkServiceImpl extends ServiceImpl<WebsiteLinkMapper, WebsiteLink> implements WebsiteLinkService {
    @Override
    public PageResult<WebsiteLink> queryLink(WebsiteLinkQuery query) {
        LambdaQueryWrapper<WebsiteLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Objects.nonNull(query.getWebsiteId()), WebsiteLink::getWebsiteId, query.getWebsiteId());
        wrapper.like(StringUtils.hasText(query.getUrl()), WebsiteLink::getUrl, query.getUrl());
        wrapper.eq(Objects.nonNull(query.getExtLink()), WebsiteLink::getExtLink, query.getExtLink());
        wrapper.eq(Objects.nonNull(query.getUrlType()), WebsiteLink::getUrlType, query.getUrlType());
        wrapper.orderByDesc(WebsiteLink::getCreatedAt);

        Page<WebsiteLink> websiteLinkPage = baseMapper.selectPage(new Page<>(query.getPage(), query.getSize()), wrapper);
        PageResult<WebsiteLink> result = new PageResult<>();
        result.setTotal(websiteLinkPage.getTotal());
        result.setRecords(websiteLinkPage.getRecords());
        result.setSize(websiteLinkPage.getSize());
        result.setCurrent(websiteLinkPage.getCurrent());
        result.setPages(websiteLinkPage.getPages());
        return result;
    }

    @Override
    public void batchSaveWebsiteLink(List<WebsiteLink> websiteLinks) {
        baseMapper.batchInsertOrUpdate(websiteLinks);
    }
}