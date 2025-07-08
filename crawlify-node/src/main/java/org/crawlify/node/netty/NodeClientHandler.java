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

/**
 * NodeClientHandler 是 Netty 客户端处理器，负责与服务端通信。
 * 主要功能包括：
 * - 节点注册（REGISTER）
 * - 心跳发送（HEARTBEAT）
 * - 任务分配处理（TASK_ASSIGN）
 * - 任务停止处理（STOP）
 * - 异步任务完成通知（ASYNC_TASK）
 *
 * 使用 Spring 管理生命周期，并集成 Redis、WebMagic 框架进行爬虫任务调度和数据持久化。
 */
@Component
public class NodeClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NodeClientHandler.class);

    /**
     * 从配置文件注入当前节点 server 端口
     */
    @Value("${server.port}")
    private Integer nodePort;

    /**
     * 保存当前连接上下文，用于后续发送消息
     */
    private ChannelHandlerContext ctx;

    /**
     * Redis 操作模板，用于分布式任务队列
     */
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * Redis 字符串操作模板
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private WebsiteInfoService websiteInfoService;


    @Resource
    private SpiderNodeHolder spiderNodeHolder;

    /**
     * 当客户端连接建立成功时触发
     * @param ctx 连接上下文
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        registerNode();
    }

    /**
     * 注册节点到服务端
     */
    private void registerNode() {
        Message message = new Message();
        message.setType(Message.MessageType.REGISTER);

        SpiderNode node = new SpiderNode();

        try {
            node.setNodeIp(IpUtils.getLocalNonLoopbackIpAddress());
        } catch (SocketException e) {
            logger.error("获取本机IP地址失败", e);
            throw new RuntimeException(e);
        }

        node.setNodePort(nodePort);
        node.setNodeId(UUID.randomUUID().toString());
        node.setStatus(1);
        // 根据IP 和 PORT 生成唯一的 nodeId (md5)
        String nodeId = UUID.nameUUIDFromBytes((node.getNodeIp() + ":" + node.getNodePort()).getBytes()).toString();
        node.setNodeId(nodeId);

        message.setNodeId(node.getNodeId());

        message.setData(node);
        logger.info("Register node: {}", node);
        spiderNodeHolder.setSpiderNode(node);
        ctx.writeAndFlush(message);
    }

    /**
     * 每30秒执行一次
     */
    @Scheduled(cron = "*/30 * * * * *")
    public void sendHeartbeat() {
        if (ctx != null && ctx.channel().isActive()) {
            Message message = new Message();
            message.setType(Message.MessageType.HEARTBEAT);
            SpiderNode spiderNode = spiderNodeHolder.getSpiderNode();
            spiderNode.setTaskCount(NodeCache.spiderTaskCache.size());
            ctx.writeAndFlush(message);
        }
    }

    /**
     * 处理接收到的消息
     * @param ctx 连接上下文
     * @param msg 接收到的消息
     */
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


    /**
     * 处理任务分配消息
     * @param message 任务分配消息
     */
    private void handleTaskAssign(Message message) {
        TaskNode taskNode = (TaskNode) message.getData();
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
        NodeEndListener nodeEndListener = new NodeEndListener(taskNode);
        spider.addSpiderEndListener(nodeEndListener);
        spider.runAsync();
    }

    /**
     * 当 Spider 完成时，调用这个方法，给 master 发送任务完成的消息（Netty方式）
     * @param taskId 任务ID
     */
    public void handleAsyncTask(String taskId) {
        if (ctx != null && ctx.channel().isActive()) {
            Message message = new Message();
            message.setType(Message.MessageType.ASYNC_TASK);
            message.setData(taskId);
            ctx.writeAndFlush(message);
            logger.info("通过Netty发送任务完成消息: {}", taskId);
        } else {
            logger.warn("Channel 不可用，无法发送任务完成消息: {}", taskId);
        }
    }

    /**
     * 当 platform 发送 Message 时， Message.Type 为 Stop，调用这个方法，停止任务，仅需停止，状态由 platform 控制
     * @param message 停止任务的消息
     */
    public void handleTaskStop(Message message) {
        String taskId = (String) message.getData();
        Spider spider = NodeCache.spiderTaskCache.get(taskId);
        if (spider != null) {
            spider.stop();
            NodeCache.spiderTaskCache.remove(taskId);
            logger.info("已停止任务: {}", taskId);
        } else {
            logger.warn("未找到需要停止的任务: {}", taskId);
        }
    }
}