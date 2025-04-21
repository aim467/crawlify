package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.entity.DynamicConfig;
import org.crawlify.common.mapper.DynamicConfigMapper;
import org.crawlify.common.service.DynamicConfigService;
import org.springframework.stereotype.Service;

@Service
public class DynamicConfigServiceImpl extends ServiceImpl<DynamicConfigMapper, DynamicConfig> implements DynamicConfigService {
}