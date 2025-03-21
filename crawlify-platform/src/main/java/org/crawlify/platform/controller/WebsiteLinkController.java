package org.crawlify.platform.controller;

import org.crawlify.platform.entity.PageResult;
import org.crawlify.platform.entity.WebsiteLink;
import org.crawlify.platform.entity.WebsiteLinkQuery;
import org.crawlify.platform.service.WebsiteLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public PageResult<WebsiteLink> list(WebsiteLinkQuery query) {
        return websiteLinkService.queryLink(query);

    }
}