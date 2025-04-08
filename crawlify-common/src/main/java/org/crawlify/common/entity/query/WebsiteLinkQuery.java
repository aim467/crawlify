package org.crawlify.common.entity.query;

import lombok.Data;

@Data
public class WebsiteLinkQuery extends Query {
    private Integer websiteId;
    private String url;
    private Boolean extLink;
    private Integer urlType;
}
