package org.crawlify.node.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebsiteLink {
    private Long id;
    private String url;
    private Integer websiteId;
    private Integer type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
