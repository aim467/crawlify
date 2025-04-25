package org.crawlify.node.config;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.crawlify.common.entity.SpiderNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

@Component
@Slf4j
public class InitTaskNode implements CommandLineRunner {

    @Value("${crawlify.master}")
    private String master;

    @Value("${server.port}")
    private int port;


    @Override
    public void run(String... args) throws Exception {
        // 获取本地ip，非回环ip
        String localIp = getLocalNonLoopbackIpAddress();
        // 获取此node端口
        int nodePort = port;

        SpiderNode spiderNode = new SpiderNode();
        spiderNode.setNodeIp(localIp);
        spiderNode.setNodePort(nodePort);

        try {
            // 发送 master + /saveNode post
            HttpUtil.createPost(master + "saveNode").contentType("application/json").body(JSON.toJSONString(spiderNode))
                    .execute().body();
            log.info("send node info to master: {}", spiderNode);
        } catch (Exception e) {
            throw new RuntimeException(String.format("cannot connect crawlify platform: %s", e.getMessage()));
        }
    }

    private String getLocalNonLoopbackIpAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress ia = inetAddresses.nextElement();
                if (!ia.isLoopbackAddress() && ia.isSiteLocalAddress()) {
                    return ia.getHostAddress();
                }
            }
        }
        return null;
    }
}