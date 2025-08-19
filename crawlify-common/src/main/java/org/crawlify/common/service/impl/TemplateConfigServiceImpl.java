package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.entity.TemplateConfig;
import org.crawlify.common.mapper.TemplateConfigMapper;
import org.crawlify.common.service.TemplateConfigService;
import org.springframework.stereotype.Service;

@Service
public class TemplateConfigServiceImpl extends ServiceImpl<TemplateConfigMapper, TemplateConfig> implements TemplateConfigService {
}