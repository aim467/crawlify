package org.crawlify.common.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.Channel;
import org.crawlify.common.cache.PlatformCache;
import org.crawlify.common.entity.SpiderNode;
import org.crawlify.common.protocol.Message;
import org.crawlify.common.service.SpiderTaskService;
import org.crawlify.common.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlatformServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PlatformServerHandler.class);


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof Message) {
            Message message = (Message) msg;
            logger.info("Received message: {}", message);
            switch (message.getType()) {
                case REGISTER:
                    handleRegister(ctx, message);
                    break;
                case HEARTBEAT:
                    handleHeartbeat(message);
                    break;
                case ASYNC_TASK:
                    handleTaskStatus(message);
                    break;
                case NODE_STATUS:
                    handleNodeStatus(message);
                default:
                    logger.warn("Unknown message type: {}", message.getType());
            }
        }
    }

    private void handleNodeStatus(Message message) {
        SpiderNode spiderNode = (SpiderNode) message.getData();
        PlatformCache.spiderNodeCache.put(spiderNode.getNodeId(), spiderNode);
    }

    private void handleRegister(ChannelHandlerContext ctx, Message message) {
        String nodeId = message.getNodeId();
        SpiderNode node = (SpiderNode) message.getData();

        // 更新节点信息
        node.setStatus(1);
        PlatformCache.spiderNodeCache.put(nodeId, node);

        // 保存Channel
        PlatformCache.channelCache.put(nodeId, ctx.channel());

        // 发送确认消息
        Message response = new Message();
        response.setType(Message.MessageType.REGISTER);
        response.setData("success");
        ctx.writeAndFlush(response);
        logger.info("Node registered: {}", nodeId);
    }

    private void handleHeartbeat(Message message) {
        String nodeId = message.getNodeId();
        SpiderNode node = PlatformCache.spiderNodeCache.get(nodeId);
        if (node != null) {
            node.setStatus(1);
            logger.debug("Received heartbeat from node: {}", nodeId);
            PlatformCache.spiderNodeCache.put(nodeId, node);
        }
    }

    private void handleTaskStatus(Message message) {
        logger.info("Received task_id: {}", message.getData());
        SpiderTaskService spiderTaskService = SpringContextUtil.getBean(SpiderTaskService.class);
        spiderTaskService.asyncTaskStatus(message.getData().toString());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // 节点断开连接时，更新节点状态
        String nodeId = getNodeIdByChannel(ctx.channel());
        if (nodeId != null) {
            SpiderNode node = PlatformCache.spiderNodeCache.get(nodeId);
            if (node != null) {
                node.setStatus(0);
            }
            PlatformCache.channelCache.remove(nodeId);
            logger.info("Node disconnected: {}", nodeId);
        }
    }

    private String getNodeIdByChannel(Channel channel) {
        for (Map.Entry<String, Channel> entry : PlatformCache.channelCache.entrySet()) {
            if (entry.getValue() == channel) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Channel getNodeChannel(String nodeId) {
        return PlatformCache.channelCache.get(nodeId);
    }
}