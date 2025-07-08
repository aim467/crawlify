package org.crawlify.platform.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import org.crawlify.common.cache.PlatformCache;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.entity.result.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import us.codecraft.webmagic.Spider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class IndexController {

    @Value("${crawlify.username}")
    private String username;

    @Value("${crawlify.password}")
    private String password;

    /**
     * 手动刷新节点信息
     * @return
     */
    @PostMapping("/refreshNode")
    public R<List<SpiderNode>> refreshNode() {
        PlatformCache.spiderNodeCache.values().forEach(node -> {
            String nodeId = node.getNodeId();
            String nodeIp = node.getNodeIp();
            int nodePort = node.getNodePort();
            String url = "http://" + nodeIp + ":" + nodePort + "/status";
            String response = HttpUtil.get(url);
            R<SpiderNode> ret = JSON.parseObject(response, R.class);
            PlatformCache.spiderNodeCache.put(nodeId, ret.getData());
        });
        List<SpiderNode> collect = new ArrayList<>(PlatformCache.spiderNodeCache.values());
        return R.ok(collect);
    }

    @GetMapping("/nodeList")
    public R<List<SpiderNode>> nodeList() {
        List<SpiderNode> spiderNodes = new ArrayList<>(PlatformCache.spiderNodeCache.values());
        return R.ok(spiderNodes);
    }


    // 使用 sa-token 写登录
    @PostMapping("/login")
    public R login() {
        if (username.equals("admin") && password.equals("123456")) {
            StpUtil.login("admin");
            Map<String, Object> result = new HashMap<>();
            result.put("token", StpUtil.getTokenValue());
            return R.ok(result);
        }
        return R.fail("账号密码有误");
    }

    @PostMapping("/logout")
    public R logout() {
        StpUtil.logout();
        return R.ok();
    }
}