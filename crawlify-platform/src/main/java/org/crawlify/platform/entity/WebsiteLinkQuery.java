package org.crawlify.platform.entity;

import lombok.Data;

@Data
public class WebsiteLinkQuery extends Query {
    private Integer websiteId;
    private String url;
}
