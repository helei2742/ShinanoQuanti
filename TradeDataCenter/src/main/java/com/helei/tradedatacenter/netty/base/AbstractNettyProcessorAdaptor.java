package com.helei.tradedatacenter.netty.base;


import com.helei.tradedatacenter.entity.WebSocketCommand;
import com.helei.tradedatacenter.netty.NettyConstants;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.UUID;


/**
 * 处理broker与client之间的心跳
 */
public abstract class AbstractNettyProcessorAdaptor extends SimpleChannelInboundHandler<WebSocketCommand> implements NettyBaseHandler {

    protected int heartbeatCount = 0;

    public NettyClientEventHandler eventHandler;

    private WebSocketClientHandshaker handshaker;

    private ChannelPromise handshakeFuture;

    public AbstractNettyProcessorAdaptor() {
        this.eventHandler = new NettyClientEventHandler() {
            @Override
            public void exceptionHandler(ChannelHandlerContext ctx, Throwable cause) {
            }
        };
    }

    public AbstractNettyProcessorAdaptor(NettyClientEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.attr(NettyConstants.ATTRIBUTE_KEY).set(UUID.randomUUID().toString());
        handshaker.handshake(ctx.channel());
        eventHandler.activeHandler(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, WebSocketCommand webSocketCommand) throws Exception {
        Channel ch = context.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) webSocketCommand);
            handshakeFuture.setSuccess();
            System.out.println("WebSocket Handshake completed!");
            return;
        }
    }

    protected void handlePing(ChannelHandlerContext context, WebSocketCommand webSocketCommand) {
        sendPongMsg(context);
    }

    protected void handlePong(ChannelHandlerContext context, WebSocketCommand webSocketCommand) {
        printLog(String.format("get pong msg from [%s][%s] ",
                context.channel().attr(NettyConstants.ATTRIBUTE_KEY).get(),
                context.channel().remoteAddress()));
    }

    protected abstract void handlerMessage(ChannelHandlerContext context, WebSocketCommand webSocketCommand);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        eventHandler.exceptionHandler(ctx, cause);
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
     * 设备下线处理
     *
     * @param ctx ctx
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        eventHandler.closeHandler(ctx.channel());
    }

    /**
     * 超过限定时间channel没有读写时触发
     *
     * @param ctx ctx
     */
    protected void handleAllIdle(ChannelHandlerContext ctx) {
    }

    public void sendPingMsg(ChannelHandlerContext context) {

        printLog(String.format("send ping msg to [%s], hear beat count [%d]",
                context.channel().remoteAddress(), heartbeatCount++));
    }

    public void sendPongMsg(ChannelHandlerContext context) {


        printLog(String.format("send pong msg to [%s], hear beat count [%d]",
                context.channel().remoteAddress(), heartbeatCount++));
    }

    @Override
    public void sendMsg(ChannelHandlerContext context, WebSocketCommand webSocketCommand) {
        context.channel().writeAndFlush(webSocketCommand);
    }
}
