package com.helei.tradedatacenter;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;


//客户端业务处理类
@Slf4j
public class ClientHandler   extends SimpleChannelInboundHandler<Object> {

    private ChannelHandlerContext channel;
    private  WebSocketClientHandshaker handshaker;
    private  ChannelPromise handshakeFuture;

    /**
     * 当客户端主动链接服务端的链接后，调用此方法
     *
     * @param channelHandlerContext ChannelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        System.out.println("客户端Active .....");
        handlerAdded(channelHandlerContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("\n\t⌜⎓⎓⎓⎓⎓⎓exception⎓⎓⎓⎓⎓⎓⎓⎓⎓\n" +
                cause.getMessage());
        ctx.close();
    }

    public void setHandshaker(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }
    public ChannelFuture handshakeFuture() {
        return this.handshakeFuture;
    }

//    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("channelRead0");
//        Channel ch = ctx.channel();
//        if (!handshaker.isHandshakeComplete()) {
//            try {
//                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
//                System.out.println("WebSocket Client connected!");
//                handshakeFuture.setSuccess();
//            } catch (WebSocketHandshakeException e) {
//                System.out.println("WebSocket Client failed to connect");
//                handshakeFuture.setFailure(e);
//
//            }
//            return;
//        }
//        if (msg instanceof FullHttpResponse) {
//            FullHttpResponse response = (FullHttpResponse) msg;
//            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.getStatus() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
//        }
//
//        WebSocketFrame frame = (WebSocketFrame) msg;
//        if (frame instanceof TextWebSocketFrame) {
//            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;            // resposnse(ctx, frame);
//            channel.writeAndFlush(textFrame.text());
//            System.out.println("WebSocket Client received message: " + textFrame.text());
//        } else if (frame instanceof PongWebSocketFrame) {
//            System.out.println("WebSocket Client received pong");
//        } else if (frame instanceof CloseWebSocketFrame) {
//            System.out.println("WebSocket Client received closing");
//            ch.close();
//        }
//
//    }

//    protected void channelRead0(ChannelHandlerContext ctx, Object o) throws Exception {
//        System.out.println("channelRead0");
//
//        // 握手协议返回，设置结束握手
//        if (!this.handshaker.isHandshakeComplete()){
//            FullHttpResponse response = (FullHttpResponse)o;
//            this.handshaker.finishHandshake(ctx.channel(), response);
//            this.handshakeFuture.setSuccess();
//            System.out.println("WebSocketClientHandler::channelRead0 HandshakeComplete...");
//            return;
//        }
//        else  if (o instanceof TextWebSocketFrame)
//        {
//            TextWebSocketFrame textFrame = (TextWebSocketFrame)o;
//            System.out.println("WebSocketClientHandler::channelRead0 textFrame: " + textFrame.text());
//        } else   if (o instanceof CloseWebSocketFrame){
//            System.out.println("WebSocketClientHandler::channelRead0 CloseWebSocketFrame");
//        }
//
//    }

    protected void channelRead0(ChannelHandlerContext ctx, Object message) throws Exception {
        System.out.println("channelRead0");

        // 判断是否正确握手
        if (!this.handshaker.isHandshakeComplete()){
            try {
                this.handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) message);
                log.debug("websocket Handshake 完成!");
                this.handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                log.debug("websocket连接失败!");
                this.handshakeFuture.setFailure(e);
            }
            return;

        }
        // 握手失败响应
        if (message instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) message;
            log.error("握手失败！code:{},msg:{}", response.status(), response.content().toString(CharsetUtil.UTF_8));
        }
        WebSocketFrame frame = (WebSocketFrame) message;
        // 消息处理
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            log.debug("收到消息: " + textFrame.text());
        }
        if (frame instanceof PongWebSocketFrame) {
            log.debug("pong消息");
        }
        if (frame instanceof CloseWebSocketFrame) {
            log.debug("服务器主动关闭连接");
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("超时事件时触发");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 当我们长时间没有给服务器发消息时，发送ping消息，告诉服务器我们还活跃
            if (event.state().equals(IdleState.WRITER_IDLE)) {
                log.debug("发送心跳");
                ctx.writeAndFlush(new PingWebSocketFrame());
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
