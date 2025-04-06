package org.crawlify.common.entity.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WebsiteInfoQuery extends Query {
    private String name;
    private String baseUrl;
    private String domain;
}
