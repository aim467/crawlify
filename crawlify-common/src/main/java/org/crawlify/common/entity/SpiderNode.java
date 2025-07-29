package org.crawlify.common.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpiderNode implements Serializable {

    private String nodeId;

    private String nodeIp;

    private Integer nodePort;

    /**
     * 1 正常
     * 0 离线
     */
    private Integer status;

    private Integer taskCount;

    private Integer threadNum;
}
