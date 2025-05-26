package org.crawlify.node.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.protocol.Message;
import org.crawlify.common.utils.IpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.SocketException;
import java.util.UUID;

@Component
public class NodeClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NodeClientHandler.class);

    @Value("${node.id}")
    private String nodeId;

    @Value("${server.port}")
    private int nodePort;

    private ChannelHandlerContext ctx;

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

        node.setNodePort(nodePort);
        node.setNodeId(UUID.randomUUID().toString());
        node.setStatus(1);
        message.setNodeId(node.getNodeId());

        message.setData(node);
        logger.info("Register node: {}", node);
        ctx.writeAndFlush(message);
    }

    @Scheduled(fixedRate = 10000)
    public void sendHeartbeat() {
        if (ctx != null && ctx.channel().isActive()) {
            Message message = new Message();
            message.setType(Message.MessageType.HEARTBEAT);
            message.setNodeId(nodeId);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            switch (message.getType()) {
                case TASK_ASSIGN:
                    handleTaskAssign(message);
                    break;
                default:
                    logger.warn("Unknown message type: {}", message.getType());
            }
        }
    }

    private void handleTaskAssign(Message message) {
        // 处理任务分配
    }


}