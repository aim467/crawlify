package org.crawlify.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.mapper.WebsiteInfoMapper;
import org.crawlify.common.service.WebsiteInfoService;
import org.springframework.stereotype.Service;

@Service
public class WebsiteInfoServiceImpl extends ServiceImpl<WebsiteInfoMapper, WebsiteInfo> implements WebsiteInfoService {
    // 可以在此处实现自定义的服务方法
}