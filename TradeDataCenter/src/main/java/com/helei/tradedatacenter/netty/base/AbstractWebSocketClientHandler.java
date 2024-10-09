package com.helei.tradedatacenter.netty.base;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import io.netty.channel.*;


@Slf4j
public abstract class AbstractWebSocketClientHandler<P,T> extends SimpleChannelInboundHandler<Object> {

    private ChannelPromise handshakeFuture;

    protected AbstractWebsocketClient<P, T> websocketClient;

    /**
     * 收到消息处理,转换为对象
     * @param text 消息字符串
     */
    protected abstract T messageConvert(String text);

    /**
     * 从response里取id
     * @param message message
     * @return id
     */
    protected abstract String getIdFromMessage(T message);

    /**
     * 从response里取id
     * @param command command
     * @return id
     */
    protected abstract String getIdFromCommand(P command);

    /**
     * 获取ping
     * @return ping
     */
    protected abstract P getPing();

    /**
     * 获取pong
     * @return pong
     */
    protected abstract P getPong();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {

        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("WebSocket Client connected!");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();

        if (msg instanceof FullHttpResponse response) {
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }  else if (msg instanceof WebSocketFrame frame) {
            if (frame instanceof TextWebSocketFrame textFrame) {

                log.info("websocket client 接收到的消息：{}",textFrame.text());
                T message = messageConvert(textFrame.text());

                String id = getIdFromMessage(message);

                /**
                 * 尝试提交失败，回过去一个pong
                 */
                if (!websocketClient.submitResponse(id, message)) {
                    websocketClient.sendRequest(getPong());
                }

            } else if (frame instanceof PongWebSocketFrame) {
                log.info("WebSocket Client received pong");
            } else if (frame instanceof CloseWebSocketFrame) {
                log.info("websocket client关闭");
                ch.close();
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
        sendPing();
    }

    private void sendPing() {
        P ping = getPing();
        String id = getIdFromCommand(ping);
        log.info("send ping, request id[{}}", id);
        websocketClient.sendRequest(id, ping, response -> {
            log.info("get pong, request id[{}}", id);
        });
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }
}
