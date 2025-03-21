package org.crawlify.node.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.crawlify.node.entity.WebsiteInfo;
import org.crawlify.node.mapper.WebsiteInfoMapper;
import org.crawlify.node.service.WebsiteInfoService;
import org.springframework.stereotype.Service;

@Service
public class WebsiteInfoServiceImpl extends ServiceImpl<WebsiteInfoMapper, WebsiteInfo> implements WebsiteInfoService {

}