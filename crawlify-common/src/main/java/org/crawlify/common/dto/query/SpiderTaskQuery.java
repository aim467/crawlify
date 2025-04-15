package org.crawlify.common.dto.query;

import lombok.Data;

@Data
public class SpiderTaskQuery extends Query {
    private String websiteName;
    private Integer status;
}
