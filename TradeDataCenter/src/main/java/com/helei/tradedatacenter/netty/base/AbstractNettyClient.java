package com.helei.tradedatacenter.netty.base;


import com.helei.tradedatacenter.netty.AbstractWSClientHandler;
import com.helei.tradedatacenter.netty.NettyConstants;
import com.helei.tradedatacenter.netty.handler.WebSocketCommandDecoder;
import com.helei.tradedatacenter.netty.handler.WebSocketCommandEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
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
import lombok.extern.slf4j.Slf4j;


import java.net.URI;


@Slf4j
public class AbstractNettyClient {

    /**
     * 订阅的uri
     */
    protected String uri;

    /**
     * 连接的channel
     */
    private Channel channel;

    /**
     * 连接的断开时间
     */
    private final int idleTimeSeconds;

    /**
     * 接受、发送消息的处理器适配器
     */
    private final AbstractNettyProcessorAdaptor nettyProcessorAdaptor;


    public AbstractNettyClient(String uri, Integer idleTimeSeconds, AbstractNettyProcessorAdaptor nettyProcessorAdaptor) {
        this.uri = uri;
        this.idleTimeSeconds = idleTimeSeconds;
        this.nettyProcessorAdaptor = nettyProcessorAdaptor;
    }

    public void connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            URI websocketURI = new URI(uri);
            String scheme = websocketURI.getScheme() == null ? "ws" : websocketURI.getScheme();
            String host = websocketURI.getHost();
            int port = websocketURI.getPort() == -1 ? 80 : websocketURI.getPort();

            final SslContext sslCtx;
            if ("wss".equalsIgnoreCase(scheme)) {
                sslCtx = SslContextBuilder.forClient()
                        .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            } else {
                sslCtx = null;
            }

            AbstractWSClientHandler handler = new AbstractWSClientHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(
                            websocketURI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override//链接建立后被调用，进行初始化
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new IdleStateHandler(0, 0, idleTimeSeconds));

                            ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(NettyConstants.MAX_FRAME_LENGTH, 0, 4, 0, 4));
                            ch.pipeline().addLast(new LengthFieldPrepender(4));

                            ch.pipeline().addLast(new WebSocketCommandEncoder());
                            ch.pipeline().addLast(new WebSocketCommandDecoder());

                            ch.pipeline().addLast(nettyProcessorAdaptor);
                        }
                    });

            ChannelFuture future = b.connect(host, port).sync();
            channel = future.channel();
            handler.handshakeFuture().sync();
        } finally {
            // 这里可以根据需要关闭 group 或者保留连接
            // group.shutdownGracefully();
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
}
