package org.crawlify.platform.entity;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records = Collections.emptyList();

    private Long total;

    private Long size;

    private Long current;

    private Long pages;
}
