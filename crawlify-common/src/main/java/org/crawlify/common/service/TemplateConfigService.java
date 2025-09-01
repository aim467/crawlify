package org.crawlify.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.crawlify.common.entity.TemplateConfig;
import org.crawlify.common.entity.result.R;

public interface TemplateConfigService extends IService<TemplateConfig> {
    R saveScript(TemplateConfig templateConfig);

    R runScript(TemplateConfig templateConfig);

    R stopScript(TemplateConfig templateConfig);

    R<String> getScript(String configId);
}