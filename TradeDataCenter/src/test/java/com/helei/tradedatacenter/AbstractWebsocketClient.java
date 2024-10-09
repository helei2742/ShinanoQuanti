package com.helei.tradedatacenter;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * This is an example of a WebSocket client.
 * <p>
 * In order to run this example you need a compatible WebSocket server.
 * Therefore you can either start the WebSocket server from the examples
 * by running { io.netty.example.http.websocketx.server.WebSocketServer}
 * or connect to an existing WebSocket server such as
 * <a href="https://www.websocket.org/echo.html">ws://echo.websocket.org</a>.
 * <p>
 * The client will attempt to connect to the URI passed to it as the first argument.
 * You don't have to specify any arguments if you want to connect to the example WebSocket server,
 * as this is the default.
 */
@Slf4j
public class AbstractWebsocketClient {

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
        resolveParamFromUrl();
    }


    public void startClient() throws Exception {
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
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
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
                            p.addLast(
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(8192),
                                    WebSocketClientCompressionHandler.INSTANCE,
                                    handler
                            );
                        }
                    });

            channel = b.connect(uri.getHost(), port).sync().channel();

            handler.handshakeFuture().sync();
            // 进行注册
            channel.writeAndFlush("发送的第一条消息");
            // 在这里可以添加一个默认心跳 15s发一次
            channel.eventLoop().scheduleAtFixedRate(() -> {
                channel.writeAndFlush(new TextWebSocketFrame("pong"));
            }, 0, 15, TimeUnit.SECONDS);

            //如果下面代码，则main方法所在的线程，即主线程会在执行完bind().sync()方法后，会进入finally 代码块，之前的启动的nettyserver也会随之关闭掉，整个程序都结束了
            channel.closeFuture().sync();
        } catch (Exception e) {
//            channel.close();
            log.error("websocket client 启动失败，失败原因：{}", e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
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
