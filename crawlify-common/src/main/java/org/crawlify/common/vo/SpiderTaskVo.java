package org.crawlify.common.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SpiderTaskVo {
    private String taskId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String websiteName;
}

//   private Integer websiteId;