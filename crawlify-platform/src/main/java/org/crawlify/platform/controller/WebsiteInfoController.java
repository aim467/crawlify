package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.dto.query.WebsiteInfoQuery;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.WebsiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/websiteInfo")
public class WebsiteInfoController {

    @Autowired
    private WebsiteInfoService websiteInfoService;

    @PostMapping
    public R<Boolean> save(@RequestBody WebsiteInfo websiteInfo) {
        websiteInfo.setUpdatedAt(LocalDateTime.now());
        return R.ok(websiteInfoService.save(websiteInfo));
    }

    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        return R.ok(websiteInfoService.removeById(id));
    }

    @PutMapping
    public R<Boolean> update(@RequestBody WebsiteInfo websiteInfo) {
        websiteInfo.setUpdatedAt(LocalDateTime.now());
        return R.ok(websiteInfoService.updateById(websiteInfo));
    }

    @GetMapping("/{id}")
    public R<WebsiteInfo> getById(@PathVariable Long id) {
        return R.ok(websiteInfoService.getById(id));
    }

    @GetMapping("/list")
    public R<Page<WebsiteInfo>> list(WebsiteInfoQuery query) {
        LambdaQueryWrapper<WebsiteInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), WebsiteInfo::getName, query.getName());
        wrapper.like(StringUtils.hasText(query.getBaseUrl()), WebsiteInfo::getBaseUrl, query.getBaseUrl());
        wrapper.like(StringUtils.hasText(query.getDomain()), WebsiteInfo::getDomain, query.getDomain());
        Page<WebsiteInfo> page = websiteInfoService.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return R.ok(page);
    }
}