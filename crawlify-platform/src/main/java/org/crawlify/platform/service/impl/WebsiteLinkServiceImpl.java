package org.crawlify.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.crawlify.platform.entity.PageResult;
import org.crawlify.platform.entity.WebsiteLink;
import org.crawlify.platform.entity.WebsiteLinkQuery;
import org.crawlify.platform.mapper.WebsiteLinkMapper;
import org.crawlify.platform.service.WebsiteLinkService;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class WebsiteLinkServiceImpl extends ServiceImpl<WebsiteLinkMapper, WebsiteLink> implements WebsiteLinkService {
    @Override
    public PageResult<WebsiteLink> queryLink(WebsiteLinkQuery query) {
        LambdaQueryWrapper<WebsiteLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Objects.nonNull(query.getWebsiteId()), WebsiteLink::getWebsiteId, query.getWebsiteId());
        wrapper.like(StringUtils.isNotBlank(query.getUrl()), WebsiteLink::getUrl, query.getUrl());
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
}