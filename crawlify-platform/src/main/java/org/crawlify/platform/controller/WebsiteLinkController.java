package org.crawlify.platform.controller;

import cn.idev.excel.FastExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.result.PageResult;
import org.crawlify.common.entity.WebsiteLink;
import org.crawlify.common.dto.query.WebsiteLinkQuery;
import org.crawlify.common.entity.result.R;
import org.crawlify.common.excel.WebsiteLinkExcel;
import org.crawlify.common.excel.handler.ExtLinkConverter;
import org.crawlify.common.excel.handler.UrlTypeConverter;
import org.crawlify.common.service.WebsiteLinkService;
import org.crawlify.platform.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.write.metadata.WriteSheet;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

@Slf4j
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
        // 修正：文件名仅保留一次 .xlsx 后缀
        String fileName = "website_links_" + query.getWebsiteId() + "_" + UUID.randomUUID() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 修正：去掉重复的 .xlsx
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);
        try {
            List<WebsiteLinkExcel> links = websiteLinkService.exportLinkExcel(query);
            links = CollectionUtils.isEmpty(links) ? Collections.emptyList() : links;

            // 使用 try-with-resources 显式管理流（若 FastExcel 支持）
            // 或调整 autoCloseStream 为 FALSE（需手动关闭流）
            FastExcel.write(response.getOutputStream(), WebsiteLinkExcel.class)
                    .registerConverter(new UrlTypeConverter())
                    .registerConverter(new ExtLinkConverter())
                    .autoCloseStream(Boolean.TRUE) // 保持自动关闭（默认可能已正确处理）
                    .sheet("链接列表")
                    .doWrite(links);

        } catch (Exception e) {
            log.error("导出失败：{}", e.getMessage());
            throw new SystemException("导出失败：" + e.getMessage());
        }
    }
}