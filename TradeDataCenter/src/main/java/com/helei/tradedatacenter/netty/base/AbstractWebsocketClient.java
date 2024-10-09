
package com.helei.tradedatacenter.netty.base;


import com.helei.tradedatacenter.netty.serializer.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public abstract class AbstractWebsocketClient {

    private final String url;

    private final AbstractWebSocketClientHandler handler;

    private URI uri;

    private String host;

    private int port;

    private boolean useSSL;

    private Channel channel;

    public AbstractWebsocketClient(
            String url,
            AbstractWebSocketClientHandler handler
    ) throws URISyntaxException {
        this.url = url;
        this.handler = handler;
        this.handler.websocketClient = this;

        resolveParamFromUrl();
    }

    public void connect() throws Exception {
        log.info("websocket client 连接中....");

        final SslContext sslCtx;
        if (useSSL) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            WebSocketClientHandshaker handshake = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

            handler.init(handshake);

            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                            }
                            p.addLast(new HttpClientCodec());
                            p.addLast(new HttpObjectAggregator(8192));
                            p.addLast(WebSocketClientCompressionHandler.INSTANCE);
                            p.addLast(handler);
                        }
                    });

            channel = b.connect(uri.getHost(), port).sync().channel();

            handler.handshakeFuture().sync();

//            // 在这里可以添加一个默认心跳 15s发一次
//            channel.eventLoop().scheduleAtFixedRate(() -> {
//                channel.writeAndFlush(new TextWebSocketFrame("pong"));
//            }, 0, 15, TimeUnit.SECONDS);

            channel.closeFuture().sync();
        } catch (Exception e) {
//            channel.close();
            log.error("websocket client 启动失败，失败原因：{}", e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
    }

    /**
     * 发送消息
     * @param message message
     */
    public void sendMessage(Object message) {
        channel.writeAndFlush(Serializer.Algorithm.JSON.serialize(message));
    }



    /**
     * 解析参数
     * @throws URISyntaxException url解析错误
     */
    private void resolveParamFromUrl() throws URISyntaxException {
        uri = new URI(url);
        String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
        host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
        if (uri.getPort() == -1) {
            if ("ws".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("wss".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            log.error("Only WS(S) is supported.");
            throw new IllegalArgumentException("url error, Only WS(S) is supported.");
        }

        useSSL = "wss".equalsIgnoreCase(scheme);
    }
}
