package com.helei.tradedatacenter.netty.base;


import com.helei.tradedatacenter.netty.WebSocketClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;


import java.net.URI;


@Slf4j
public class AbstractNettyClient {
    protected String uri;
    private Channel channel;

    public AbstractNettyClient(String uri) {
        this.uri = uri;
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

            WebSocketClientHandler handler = new WebSocketClientHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(
                            websocketURI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()));

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new WebSocketClientInitializer(sslCtx, handler));

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
