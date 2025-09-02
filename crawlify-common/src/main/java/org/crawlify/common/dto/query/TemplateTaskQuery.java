package org.crawlify.common.dto.query;

import lombok.Data;

@Data
public class TemplateTaskQuery extends Query {

    private Integer status;

    private String taskId;

}
