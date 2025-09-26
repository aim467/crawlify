package org.crawlify.platform.controller;


import org.crawlify.common.entity.TemplateConfig;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.service.TemplateConfigService;
import org.crawlify.common.utils.CrawlifyPluginUtils;
import org.crawlify.plugin.CrawlifyPlugin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crawlify/plugin")
public class CrawlifyPluginController {

    private CrawlifyPluginUtils crawlifyPluginUtils = new CrawlifyPluginUtils("D:/script/jar");

    @Resource
    private TemplateConfigService templateConfigService;

    @GetMapping("/list")
    public R<List<CrawlifyPlugin>> list() {
        return R.ok(crawlifyPluginUtils.listPlugins());
    }


    @GetMapping("/run")
    public R<Map<String, Object>> run(String pluginId, String configId) {
        TemplateConfig templateConfig = templateConfigService.getById(configId);
        if (templateConfig == null) {
            return R.fail("Template config not found.");
        }
        return R.ok(crawlifyPluginUtils.runPlugin(pluginId, templateConfig));
    }


    @GetMapping("/upload")
    public R<String> upload(String fileName) {
        return R.fail();
    }
}
