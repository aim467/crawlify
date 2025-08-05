package org.crawlify.platform.controller;

import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.WebsiteLink;
import org.crawlify.common.dto.query.WebsiteLinkQuery;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.excel.handler.ExtLinkConverter;
import org.crawlify.common.excel.handler.UrlTypeConverter;
import org.crawlify.common.service.WebsiteLinkService;
import org.crawlify.platform.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.write.metadata.WriteSheet;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/websiteLink")
public class WebsiteLinkController {

    @Autowired
    private WebsiteLinkService websiteLinkService;

    @PostMapping
    public boolean save(@RequestBody WebsiteLink websiteLink) {
        return websiteLinkService.save(websiteLink);
    }

    @PutMapping
    public boolean update(@RequestBody WebsiteLink websiteLink) {
        return websiteLinkService.updateById(websiteLink);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return websiteLinkService.removeById(id);
    }

    @GetMapping("/{id}")
    public WebsiteLink getById(@PathVariable Long id) {
        return websiteLinkService.getById(id);
    }

    @GetMapping("/list")
    public R<PageResult<WebsiteLink>> list(WebsiteLinkQuery query) {
        PageResult<WebsiteLink> websiteLinkPageResult = websiteLinkService.queryLink(query);
        return R.ok(websiteLinkPageResult);

    }

    @PostMapping("/export")
    public void exportLinks(@RequestBody WebsiteLinkQuery query, HttpServletResponse response) throws URISyntaxException {
        if (query.getWebsiteId() == null) {
            throw new SystemException("请选择要导出的网站");
        }
        String fileName = "website_links_" + query.getWebsiteId() + "_" + UUID.randomUUID() + ".xlsx";
        response.reset();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

        try {

            LambdaQueryWrapper<WebsiteLink> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Objects.nonNull(query.getWebsiteId()), WebsiteLink::getWebsiteId, query.getWebsiteId());
            wrapper.like(StringUtils.hasText(query.getUrl()), WebsiteLink::getUrl, query.getUrl());
            wrapper.eq(Objects.nonNull(query.getExtLink()), WebsiteLink::getExtLink, query.getExtLink());
            wrapper.eq(Objects.nonNull(query.getUrlType()), WebsiteLink::getUrlType, query.getUrlType());
            wrapper.lt(StringUtils.hasText(query.getStartTime()), WebsiteLink::getCreatedAt, query.getStartTime());
            wrapper.gt(StringUtils.hasText(query.getEndTime()), WebsiteLink::getCreatedAt, query.getEndTime());
            wrapper.orderByDesc(WebsiteLink::getCreatedAt);

            List<WebsiteLink> links = websiteLinkService.list(wrapper);


            FastExcel.write(response.getOutputStream(), WebsiteLink.class)
                    .registerConverter(new UrlTypeConverter())
                    .registerConverter(new ExtLinkConverter())
                    .sheet("链接列表")
                    .doWrite(links);

        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        }
    }
}