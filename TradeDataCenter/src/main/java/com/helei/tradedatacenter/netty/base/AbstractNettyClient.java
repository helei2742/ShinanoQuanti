package com.helei.tradedatacenter.netty.base;


import com.helei.tradedatacenter.netty.AbstractWSClientHandler;
import com.helei.tradedatacenter.netty.NettyConstants;
import com.helei.tradedatacenter.netty.handler.WebSocketCommandDecoder;
import com.helei.tradedatacenter.netty.handler.WebSocketCommandEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;


import java.net.URI;


@Slf4j
public class AbstractNettyClient {
    /**
     * 处理事件的线程池
     */
    static EventLoopGroup group = new NioEventLoopGroup();

    static Bootstrap bootstrap = new Bootstrap();


    /**
     * 用于记录和管理所有客户端的channel
     */
    static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 订阅的uri
     */
    protected URI uri;

    private String scheme;
    private String host;

    private int port;
    /**
     * 连接的channel
     */
    private Channel channel;

    AbstractWSClientHandler handler;

    ChannelPromise handshakeFuture;
    /**
     * 连接的断开时间
     */
    private final int idleTimeSeconds;

    /**
     * 接受、发送消息的处理器适配器
     */
    private final AbstractNettyProcessorAdaptor nettyProcessorAdaptor;


    public AbstractNettyClient(
            URI websocketURI,
            Integer idleTimeSeconds,
            AbstractNettyProcessorAdaptor nettyProcessorAdaptor
    ) {

        this.uri = websocketURI;
        this.idleTimeSeconds = idleTimeSeconds;
        this.nettyProcessorAdaptor = nettyProcessorAdaptor;

        scheme = websocketURI.getScheme() == null ? "ws" : websocketURI.getScheme();
        host = websocketURI.getHost();
        port = websocketURI.getPort() == -1 ? 80 : websocketURI.getPort();

        handler = new AbstractWSClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                        uri, WebSocketVersion.V13,
                        null, true, new DefaultHttpHeaders())
        );


        bootstrap.group(new NioEventLoopGroup())
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override//链接建立后被调用，进行初始化
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, idleTimeSeconds));
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(NettyConstants.MAX_FRAME_LENGTH, 0, 4, 0, 4));
                        ch.pipeline().addLast(new LengthFieldPrepender(4));
//                        ch.pipeline().addLast(new WebSocketCommandEncoder());
//                        ch.pipeline().addLast(new WebSocketCommandDecoder());
                        ch.pipeline().addLast(handler);
                    }
                });
    }

    public void connect() throws Exception {
        try {
            try {
//                ChannelFuture future = bootstrap.connect(this.scheme +"://" + this.host , this.port).sync();
                ChannelFuture future = bootstrap.connect("dstream.binance.com" , 443).sync();
                this.channel = future.channel();
                clients.add(channel);
            } catch (Exception e) {
                log.error("创建channel失败", e);
            }
        }catch (Exception e) {
            log.error("连接服务失败", e);
        } finally {
            this.handshakeFuture = handler.handshakeFuture();
        }
    }

    public void sendMessage(String message) {
        channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    /**
     * 发送文本消息
     */
    void sendText(String msg) {
        channel.writeAndFlush(new TextWebSocketFrame(msg));
    }

    /**
     * 发送ping消息
     */
    void ping() {
        channel.writeAndFlush("");
    }
}
