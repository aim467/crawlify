package org.crawlify.common.dto.query;

import lombok.Data;

@Data
public class DynamicConfigQuery extends Query {

    private String configName;

    private String requestType;

    private String columnUrl;

    private Integer websiteId;
}
