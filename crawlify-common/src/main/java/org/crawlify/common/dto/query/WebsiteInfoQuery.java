package org.crawlify.common.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WebsiteInfoQuery extends Query {
    private String name;
    private String baseUrl;
    private String domain;
}
