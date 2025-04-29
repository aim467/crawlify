package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.crawlify.common.dto.query.DynamicConfigQuery;
import org.crawlify.common.dynamic.DynamicCrawler;
import org.crawlify.common.entity.DynamicConfig;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.DynamicConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dynamic-config")
public class DynamicConfigController {

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @GetMapping("/list")
    public R<PageResult<DynamicConfig>> getAllDynamicConfigs(DynamicConfigQuery dynamicConfigQuery) {
        LambdaQueryWrapper<DynamicConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dynamicConfigQuery.getConfigName()), DynamicConfig::getConfigName,
                dynamicConfigQuery.getConfigName());
        queryWrapper.eq(StringUtils.isNotEmpty(dynamicConfigQuery.getRequestType()), DynamicConfig::getRequestType,
                dynamicConfigQuery.getRequestType());
        queryWrapper.like(StringUtils.isNotEmpty(dynamicConfigQuery.getColumnUrl()), DynamicConfig::getColumnUrl,
                dynamicConfigQuery.getColumnUrl());
        queryWrapper.eq(ObjectUtils.isNotEmpty(dynamicConfigQuery.getWebsiteId()), DynamicConfig::getWebsiteId,
                dynamicConfigQuery.getWebsiteId());
        Page<DynamicConfig> dynamicConfigPage = dynamicConfigService.page(
                new Page<>(dynamicConfigQuery.getPage(), dynamicConfigQuery.getSize()), queryWrapper);
        PageResult<DynamicConfig> pageResult = new PageResult<>();
        pageResult.setCurrent(dynamicConfigPage.getCurrent());
        pageResult.setPages(dynamicConfigPage.getPages());
        pageResult.setSize(dynamicConfigPage.getSize());
        pageResult.setRecords(dynamicConfigPage.getRecords());
        pageResult.setTotal(dynamicConfigPage.getTotal());
        return R.ok(pageResult);
    }

    @GetMapping("/{configId}")
    public DynamicConfig getDynamicConfigById(@PathVariable String configId) {
        return dynamicConfigService.getById(configId);
    }

    @PostMapping
    public boolean saveDynamicConfig(@RequestBody DynamicConfig dynamicConfig) {
        return dynamicConfigService.save(dynamicConfig);
    }

    @PutMapping("")
    public boolean updateDynamicConfig(@RequestBody DynamicConfig dynamicConfig) {
        return dynamicConfigService.updateById(dynamicConfig);
    }

    @DeleteMapping("/{configId}")
    public boolean deleteDynamicConfig(@PathVariable String configId) {
        return dynamicConfigService.removeById(configId);
    }

    @GetMapping("/test")
    public R<List<String>> dynamicConfigTest(String configId) {
        DynamicConfig byId = dynamicConfigService.getById(configId);
        // 测试设置最大翻页为一页
        byId.setPageLen(1);
        DynamicCrawler dynamicCrawler = new DynamicCrawler(byId);
        List<String> list = dynamicCrawler.crawl();
        return R.ok(list);
    }
}