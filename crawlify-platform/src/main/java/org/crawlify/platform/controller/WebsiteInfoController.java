package org.crawlify.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.crawlify.platform.entity.WebsiteInfo;
import org.crawlify.platform.service.WebsiteInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/websiteInfo")
public class WebsiteInfoController {

    @Autowired
    private WebsiteInfoService websiteInfoService;

    @PostMapping
    public boolean save(@RequestBody WebsiteInfo websiteInfo) {
        return websiteInfoService.save(websiteInfo);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return websiteInfoService.removeById(id);
    }

    @PutMapping
    public boolean update(@RequestBody WebsiteInfo websiteInfo) {
        return websiteInfoService.updateById(websiteInfo);
    }

    @GetMapping("/{id}")
    public WebsiteInfo getById(@PathVariable Long id) {
        return websiteInfoService.getById(id);
    }

    @GetMapping("/list")
    public Page<WebsiteInfo> list(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return websiteInfoService.page(new Page<>(page, size));
    }
}