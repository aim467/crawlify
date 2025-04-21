package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.crawlify.common.dto.query.DynamicConfigQuery;
import org.crawlify.common.entity.DynamicConfig;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.DynamicConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dynamic-config")
public class DynamicConfigController {

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @GetMapping("/list")
    public PageResult<DynamicConfig> getAllDynamicConfigs(DynamicConfigQuery dynamicConfigQuery) {
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
        return pageResult;
    }

    @GetMapping("/{id}")
    public DynamicConfig getDynamicConfigById(@PathVariable Long id) {
        return dynamicConfigService.getById(id);
    }

    @PostMapping
    public boolean saveDynamicConfig(@RequestBody DynamicConfig dynamicConfig) {
        return dynamicConfigService.save(dynamicConfig);
    }

    @PutMapping("/{id}")
    public boolean updateDynamicConfig(@PathVariable Long id, @RequestBody DynamicConfig dynamicConfig) {
        return dynamicConfigService.updateById(dynamicConfig);
    }

    @DeleteMapping("/{id}")
    public boolean deleteDynamicConfig(@PathVariable Long id) {
        return dynamicConfigService.removeById(id);
    }
}