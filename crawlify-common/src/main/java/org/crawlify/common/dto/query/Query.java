package org.crawlify.common.dto.query;

import lombok.Data;

@Data
public class Query {

    private Integer page;

    private Integer size;


    // 给 page 和 size 默认值
    public Query() {
        this.page = 1;
        this.size = 10;
    }
}
