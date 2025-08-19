package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.crawlify.common.dto.query.TemplateConfigQuery;
import org.crawlify.common.entity.TemplateConfig;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.TemplateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/template-config")
public class TemplateConfigController {

    @Autowired
    private TemplateConfigService templateConfigService;

    /**
     * 分页查询
     */
    @PostMapping("/list")
    public R<Page<TemplateConfig>> page(TemplateConfigQuery query) {
        LambdaQueryWrapper<TemplateConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getConfigName()), TemplateConfig::getConfigName, query.getConfigName());
        Page<TemplateConfig> pageObj = templateConfigService.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return R.ok(pageObj);
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public R<TemplateConfig> getById(@PathVariable String id) {
        return R.ok(templateConfigService.getById(id));
    }

    /**
     * 新增配置
     */
    @PostMapping
    public R<Boolean> add(@RequestBody TemplateConfig templateConfig) {
        templateConfig.setCreatedAt(LocalDateTime.now());
        templateConfig.setUpdatedAt(LocalDateTime.now());
        return R.ok(templateConfigService.save(templateConfig));
    }

    /**
     * 更新配置
     */
    @PutMapping
    public R<Boolean> update(@RequestBody TemplateConfig templateConfig) {
        templateConfig.setUpdatedAt(LocalDateTime.now());
        return R.ok(templateConfigService.updateById(templateConfig));
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable String id) {
        return R.ok(templateConfigService.removeById(id));
    }
}
