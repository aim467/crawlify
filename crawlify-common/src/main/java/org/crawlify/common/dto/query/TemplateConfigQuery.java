package org.crawlify.common.dto.query;

import lombok.Data;

@Data
public class TemplateConfigQuery extends Query {
    private String configId;
    private String configName;
}
