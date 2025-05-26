package org.crawlify.node.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.entity.TaskNode;
import org.crawlify.common.entity.WebsiteInfo;
import org.crawlify.common.protocol.Message;
import org.crawlify.common.service.TaskNodeService;
import org.crawlify.common.service.WebsiteInfoService;
import org.crawlify.common.utils.IpUtils;
import org.crawlify.node.NodeEndListener;
import org.crawlify.node.cache.NodeCache;
import org.crawlify.node.config.RedisScheduler;
import org.crawlify.node.pipeline.DatabasePipeline;
import org.crawlify.node.processor.LinkProcessor;
import org.crawlify.common.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Spider;

import javax.annotation.Resource;
import java.net.SocketException;
import java.util.List;
import java.util.UUID;

@Component
public class NodeClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NodeClientHandler.class);

    @Value("${node.id}")
    private String nodeId;

    @Value("${server.port}")
    private int nodePort;

    private ChannelHandlerContext ctx;

    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        // 注册节点
        registerNode();
    }

    private void registerNode() {
        Message message = new Message();
        message.setType(Message.MessageType.REGISTER);

        SpiderNode node = new SpiderNode();
        node.setNodeId(nodeId);

        try {
            node.setNodeIp(IpUtils.getLocalNonLoopbackIpAddress());
        } catch (SocketException e) {
            logger.error("获取本机IP地址失败", e);
            throw new RuntimeException(e);
        }

        node.setNodePort(80);
        node.setNodeId(UUID.randomUUID().toString());
        node.setStatus(1);
        message.setNodeId(node.getNodeId());

        message.setData(node);
        logger.info("Register node: {}", node);
        ctx.writeAndFlush(message);
    }

    /**
     * 每秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {
        logger.info("发送心跳包");
        if (ctx != null && ctx.channel().isActive()) {
            Message message = new Message();
            message.setType(Message.MessageType.HEARTBEAT);
            message.setNodeId(nodeId);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("Received message: {}", msg);
        if (msg instanceof Message) {
            Message message = (Message) msg;
            switch (message.getType()) {
                case TASK_ASSIGN:
                    handleTaskAssign(message);
                    break;
                case STOP:
                    handleTaskStop(message);
                    break;
                default:
                    logger.warn("Unknown message type: {}", message.getType());
            }
        }
    }

    private void handleTaskAssign(Message message) {
        // 处理任务分配
        logger.info("Received task assign: {}", message.getData());

        TaskNode taskNode = (TaskNode) message.getData();
        TaskNodeService taskNodeService = SpringContextUtil.getBean(TaskNodeService.class);

        WebsiteInfoService websiteInfoService = SpringContextUtil.getBean(WebsiteInfoService.class);

        WebsiteInfo websiteInfo = websiteInfoService.getById(taskNode.getWebsiteId());
        int bitSize = 1 << 24; // 默认 16MB
        int[] seeds = new int[]{7, 11, 13, 31, 37, 61}; // 默认 6 个哈希函数
        Spider spider = Spider.create(new LinkProcessor(websiteInfo))
                .setScheduler(new RedisScheduler(redisTemplate, stringRedisTemplate,
                        "bloom_" + taskNode.getTaskId(),
                        "queue_" + taskNode.getTaskId(), bitSize, seeds))
                .setPipelines(List.of(new DatabasePipeline()))
                .addUrl(websiteInfo.getBaseUrl())
                .thread(taskNode.getThreadNum());
        NodeCache.spiderTaskCache.put(taskNode.getTaskId(), spider);
        NodeEndListener nodeEndListener = new NodeEndListener(taskNodeService, taskNode,
                null, null);
        spider.addSpiderEndListener(nodeEndListener);
        spider.runAsync();
    }

    /**
     * 当 Spider 完成时，调用这个方法，给 master 发送任务完成的消息（Netty方式）
     *
     * @param taskId
     */
    public void handleAsyncTask(String taskId) {
        if (ctx != null && ctx.channel().isActive()) {
            Message message = new Message();
            message.setType(Message.MessageType.ASYNC_TASK);
            message.setNodeId(nodeId);
            message.setData(taskId);
            ctx.writeAndFlush(message);
            logger.info("通过Netty发送任务完成消息: {}", taskId);
        } else {
            logger.warn("Channel 不可用，无法发送任务完成消息: {}", taskId);
        }
    }

    /**
     * 当 platform 发送 Message 时， Message.Type 为 Stop，调用这个方法，停止任务
     */
    public void handleTaskStop(Message message) {
        String taskId = (String) message.getData();
        Spider spider = NodeCache.spiderTaskCache.get(taskId);
        if (spider != null) {
            spider.stop();
            NodeCache.spiderTaskCache.remove(taskId);
            logger.info("已停止任务: {}", taskId);
            // 可选：向 master 发送任务已停止的消息
//            handleAsyncTask(taskId); // 也可自定义 STOPPED 类型
        } else {
            logger.warn("未找到需要停止的任务: {}", taskId);
        }
    }
}