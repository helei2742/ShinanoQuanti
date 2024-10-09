
package com.helei.tradedatacenter.netty.base;


import com.alibaba.fastjson.JSON;
import com.helei.tradedatacenter.netty.handler.RequestResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * Websocket客户端
 * @param <P> 请求体的类型
 * @param <T> 返回值的类型
 */
@Slf4j
public abstract class AbstractWebsocketClient<P,T> {

    private final String url;

    private final AbstractWebSocketClientHandler<P,T> handler;

    private URI uri;

    private String host;

    private int port;

    private boolean useSSL;

    private Channel channel;

    private final RequestResponseHandler<T> requestResponseHandler;

    public AbstractWebsocketClient(
            String url,
            AbstractWebSocketClientHandler<P,T> handler
    ) throws URISyntaxException {
        this.url = url;
        this.handler = handler;
        this.handler.websocketClient = this;

        resolveParamFromUrl();

        requestResponseHandler = new RequestResponseHandler<>();
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
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
//                            p.addLast(new Socks5ProxyHandler(new InetSocketAddress("127.0.0.1", 7890)));

                            if (sslCtx != null) {
                                p.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), port));
                            }
                            p.addLast(new HttpProxyHandler(new InetSocketAddress("127.0.0.1", 7890)));

                            p.addLast(new HttpClientCodec());
                            p.addLast(new HttpObjectAggregator(8192));
                            p.addLast(new ChunkedWriteHandler());
                            p.addLast(new WebSocketClientProtocolHandler(
                                    WebSocketClientHandshakerFactory.newHandshaker(
                                            uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
                                    )
                            ));

                            p.addLast(handler);
                        }
                    });

            channel = b.connect(host, port).sync().channel();

            // 8. 等待 WebSocket 握手完成
            AbstractWebSocketClientHandler<P,T> handler = channel.pipeline().get(AbstractWebSocketClientHandler.class);

            handler.handshakeFuture().sync();

            // 发送 WebSocket 帧
            channel.writeAndFlush(new TextWebSocketFrame("Hello WebSocket Server through HTTP Proxy and SSL!"));

            channel.closeFuture().sync();
        } catch (Exception e) {
//            close();
            log.error("websocket client 启动失败", e);
        }
//        finally {
//            group.shutdownGracefully();
//        }
    }

    /**
     * 发送请求, 注册响应监听
     * @param id id
     * @param request 请求体
     * @param callback 请求结果的回调
     */
    public void sendRequest(String id, P request, Consumer<T> callback){
        boolean flag = requestResponseHandler.registryRequest(id, callback);

         if (flag) {
             channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(request)));
         } else {
             throw new IllegalArgumentException("request id registered");
         }
    }


    /**
     * 发送请求,不组册监听
     * @param request 请求体
     */
    public void sendRequest(P request){
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(request)));
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

    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public boolean submitResponse(String id, T response) {
        return requestResponseHandler.submitResponse(id, response);
    }
}
