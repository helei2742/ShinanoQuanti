package com.helei.netty.base;

import com.helei.netty.NettyConstants;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * WebSocket客户端处理器基础类
 * 处理连接握手
 *
 * @param <P>
 * @param <T>
 */
@Slf4j
@ChannelHandler.Sharable
public abstract class BaseWebSocketClientHandler<P, T> extends SimpleChannelInboundHandler<Object> {
    private WebSocketClientHandshaker handshaker;

    private ChannelPromise handshakeFuture;

    protected AbstractWebsocketClient<P, T> websocketClient;

    /**
     * 收到消息处理
     *
     * @param text 消息字符串
     */
    protected abstract void whenReceiveMessage(String text);


    public void init(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        log.info("WebSocket Client [{}] connected!", websocketClient.getName());
        channel.attr(NettyConstants.CLIENT_NAME).set(websocketClient.getName());
        handshaker.handshake(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.warn("WebSocket Client [{}] disconnected!", ctx.channel().attr(NettyConstants.CLIENT_NAME).get());

        websocketClient.close();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.warn("WebSocket Client [{}] unregistered!, start reconnect", ctx.channel().attr(NettyConstants.CLIENT_NAME).get());

        websocketClient.reconnect();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        // 如果握手未完成，处理 FullHttpResponse
        if (!handshaker.isHandshakeComplete()) {
            if (msg instanceof FullHttpResponse response) {
                try {
                    handshaker.finishHandshake(ch, response);
                    log.info("WebSocket client [{}] Handshake complete!", ch.attr(NettyConstants.CLIENT_NAME).get());
                    handshakeFuture.setSuccess();
                } catch (WebSocketHandshakeException e) {
                    log.info("WebSocket client [{}] Handshake failed!", ch.attr(NettyConstants.CLIENT_NAME).get());
                    handshakeFuture.setFailure(e);
                }
                return;
            }
        }

        if (msg instanceof FullHttpResponse response) {
            if (response.status().code() / 100 > 3) {
                throw new IllegalStateException(
                        "Unexpected FullHttpResponse (getStatus=" + response.status() +
                                ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
            }
        } else if (msg instanceof WebSocketFrame frame) {
            switch (frame) {
                case TextWebSocketFrame textFrame -> {
                    log.debug("websocket client [{}] 接收到的消息：{}", ch.attr(NettyConstants.CLIENT_NAME).get(), textFrame.text());

                    whenReceiveMessage(textFrame.text());
                }
                case PongWebSocketFrame pongWebSocketFrame ->
                        log.debug("WebSocket Client [{}] received pong", ch.attr(NettyConstants.CLIENT_NAME).get());
                case PingWebSocketFrame pingWebSocketFrame -> {
                    log.debug("WebSocket Client [{}] received ping", ch.attr(NettyConstants.CLIENT_NAME).get());
                    websocketClient.sendPong();
                }
                case CloseWebSocketFrame closeWebSocketFrame -> {
                    log.warn("websocket client关闭");
                    ch.close();
                }
                default -> {
                }
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
        log.error("业务处理错误，websocket client关闭", cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent e) {
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 超过限定时间channel没有读时触发
     *
     * @param ctx ctx
     */
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
    }

    /**
     * 超过限定时间channel没有写时触发
     *
     * @param ctx ctx
     */
    protected void handleWriterIdle(ChannelHandlerContext ctx) {
    }

    /**
     * 超过限定时间channel没有读写时触发
     *
     * @param ctx ctx
     */
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        websocketClient.sendPing();
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }
}
