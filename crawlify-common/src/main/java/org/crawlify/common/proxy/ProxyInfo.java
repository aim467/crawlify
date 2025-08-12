package org.crawlify.common.proxy;

import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Getter
public class ProxyInfo {
    // Getters and Setters
    private final String host;
    private final int port;
    // 引入状态，默认为未经测试
    @Setter
    private volatile Status status = Status.UNTESTED;

    public enum Status {
        UNTESTED, // 未经测试
        VALID,    // 可用
        INVALID   // 无效
    }


    public ProxyInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Proxy toJavaProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.host, this.port));
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
