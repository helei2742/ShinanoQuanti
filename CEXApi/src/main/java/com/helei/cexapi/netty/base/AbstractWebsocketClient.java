




package com.helei.cexapi.netty.base;


        import com.alibaba.fastjson.JSON;
        import com.helei.cexapi.netty.NettyConstants;
        import com.helei.cexapi.netty.handler.RequestResponseHandler;
        import io.netty.bootstrap.Bootstrap;
        import io.netty.channel.*;
        import io.netty.channel.nio.NioEventLoopGroup;
        import io.netty.channel.socket.nio.NioSocketChannel;
        import io.netty.handler.codec.http.DefaultHttpHeaders;
        import io.netty.handler.codec.http.HttpClientCodec;
        import io.netty.handler.codec.http.HttpObjectAggregator;
        import io.netty.handler.codec.http.websocketx.*;
        import io.netty.handler.proxy.Socks5ProxyHandler;
        import io.netty.handler.ssl.SslContext;
        import io.netty.handler.ssl.SslContextBuilder;
        import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
        import io.netty.handler.stream.ChunkedWriteHandler;
        import lombok.Setter;
        import lombok.extern.slf4j.Slf4j;

        import javax.net.ssl.SSLException;
        import java.net.InetSocketAddress;
        import java.net.URI;
        import java.net.URISyntaxException;
        import java.util.concurrent.*;
        import java.util.concurrent.atomic.AtomicBoolean;
        import java.util.concurrent.atomic.AtomicInteger;
        import java.util.concurrent.atomic.AtomicReference;
        import java.util.function.Consumer;

/**
 * Websocket客户端
 *
 * @param <P> 请求体的类型
 * @param <T> 返回值的类型
 */
@Slf4j
public abstract class AbstractWebsocketClient<P, T> {

    private static final int MAX_FRAME_SIZE = 10 * 1024 * 1024;  // 10 MB or set to your desired size

    /**
     * websocket的url字符串
     */
    private final String url;

    /**
     * netty pipeline 最后一个执行的handler
     */
    protected final AbstractWebSocketClientHandler<P, T> handler;

    /**
     * 执行回调的线程池
     */
    protected final ExecutorService callbackInvoker;

    /**
     * 代理
     */
    @Setter
    protected InetSocketAddress proxy = null;

    /**
     * 重链接次数
     */
    private final AtomicInteger reconnectTimes = new AtomicInteger(0);

    private Bootstrap bootstrap;

    private EventLoopGroup eventLoopGroup;

    private URI uri;

    private String host;

    private int port;

    private boolean useSSL;

    private Channel channel;

    private final RequestResponseHandler<T> requestResponseHandler;

    public AbstractWebsocketClient(
            int threadPoolSize,
            String url,
            AbstractWebSocketClientHandler<P, T> handler
    ) throws URISyntaxException, SSLException {
        this.url = url;
        this.handler = handler;
        this.handler.websocketClient = this;

        if (threadPoolSize <= 0) {
            this.callbackInvoker = null;
        } else {
            this.callbackInvoker = Executors.newFixedThreadPool(threadPoolSize);
        }

        requestResponseHandler = new RequestResponseHandler<>();

        resolveParamFromUrl();

        init();
    }

    private void init() throws SSLException {
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()
        );
        handler.init(handshaker);

        final SslContext sslCtx;
        if (useSSL) {
            sslCtx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } else {
            sslCtx = null;
        }

        bootstrap = new Bootstrap();

        eventLoopGroup = new NioEventLoopGroup();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (proxy != null) {
                            p.addLast(new Socks5ProxyHandler(proxy));
                        }

                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc(), uri.getHost(), port));
                        }

                        p.addLast("http-chunked", new ChunkedWriteHandler()); // 支持大数据流


                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(81920));
                        p.addLast(new ChunkedWriteHandler());

                        p.addLast(new WebSocketFrameAggregator(MAX_FRAME_SIZE));  // 设置聚合器的最大帧大小


                        p.addLast(handler);
                    }
                });
    }

    /**
     * 链接服务端
     * @throws Exception Exception
     */
    public CompletableFuture<Void> connect() {

        return CompletableFuture.runAsync(()->{
            AtomicBoolean isSuccess = new AtomicBoolean(false);
            while (reconnectTimes.incrementAndGet() <= NettyConstants.RECONNECT_LIMIT) {
                eventLoopGroup.schedule(()->{
                    reconnectTimes.decrementAndGet();
                }, 60, TimeUnit.SECONDS);

                log.info("start connect [{}], current times [{}]", uri,reconnectTimes.get());
                CountDownLatch latch = new CountDownLatch(1);;

                eventLoopGroup.schedule(() -> {
                    try {
                        channel = bootstrap.connect().sync().channel();
                        // 8. 等待 WebSocket 握手完成
                        handler.handshakeFuture().sync();
                        log.info("connect [{}] success, current times [{}]", uri,reconnectTimes.get());
                        isSuccess.set(true);
                    } catch (Exception e) {
                        log.error("connect [{}] error, times [{}]", uri, reconnectTimes.get(), e);
                    }
                    latch.countDown();
                }, NettyConstants.RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS);

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    log.error("connect [{}] error, times [{}]", uri, reconnectTimes.get(), e);
                }

                if (isSuccess.get()) {
                    break;
                }
            }
            if (!isSuccess.get()) {
                log.error("reconnect times out of limit [{}], close websocket client", NettyConstants.RECONNECT_LIMIT);
                close();
            }
        }, callbackInvoker);

    }


    /**
     * 关闭WebSocketClient
     */
    public void close() {
        log.info("start close websocket client");
        if (channel != null) {
            channel.close();
        }
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
        }
        log.info("web socket client closed");
    }


    /**
     * 从request获取id
     * @param request request
     * @return id
     */
    public abstract String getIdFromRequest(P request);

    /**
     * 发送请求, 注册响应监听
     * @param request  请求体
     * @param callback 请求结果的回调
     */
    public void sendRequest(P request, Consumer<T> callback) {
        sendRequest(request, callback, callbackInvoker);
    }

    /**
     * 发送请求, 注册响应监听
     *
     * @param request         请求体
     * @param callback        请求结果的回调
     * @param executorService 执行回调的线程池，传入为空则会尝试使用本类的线程池以及netty线程池
     */
    public void sendRequest(P request, Consumer<T> callback, ExecutorService executorService) {
        boolean flag = requestResponseHandler.registryRequest(getIdFromRequest(request), response -> {
            if (executorService == null) {
                if (callbackInvoker == null) { //netty线程处理
                    callback.accept(response);
                } else { //此类线程处理
                    callbackInvoker.submit(() -> {
                        callback.accept(response);
                    });
                }
            } else { //参数线程池处理
                executorService.submit(() -> {
                    callback.accept(response);
                });
            }

        });

        if (flag) {
            log.debug("send request [{}]", request);
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(request)));
        } else {
            throw new IllegalArgumentException("request id registered");
        }
    }

    /**
     * 发送请求, 注册响应监听
     * @param request         请求体
     */
    public CompletableFuture<T> sendRequest(P request) {
        return CompletableFuture.supplyAsync(()->{
            if (request == null) {
                log.error("request is null");
                return null;
            }

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<T> jb = new AtomicReference<>(null);

            boolean flag = requestResponseHandler.registryRequest(getIdFromRequest(request), response -> {
                latch.countDown();
                jb.set(response);
            });

            if (flag) {
                channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(request)));
                log.debug("send request [{}] success", request);
            } else {
                log.error("request id registered");
                return null;
            }

            try {
                if (! latch.await(NettyConstants.RECONNECT_DELAY_SECONDS, TimeUnit.SECONDS)) return null;

                return jb.get();
            } catch (InterruptedException e) {
                log.error("send request interrupted", e);
                return null;
            }
        }, callbackInvoker);
    }

    /**
     * 发送ping
     */
    public void sendPing() {
        channel.writeAndFlush(new PingWebSocketFrame());
    }

    /**
     * 发送pong
     */
    public void sendPong() {
        channel.writeAndFlush(new PongWebSocketFrame());
    }

    /**
     * 发送请求,不组册监听
     *
     * @param request 请求体
     */
    public void sendRequestNoListener(P request) {
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(request)));
    }


    /**
     * 解析参数
     *
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


    /**
     * 提交请求的响应
     *
     * @param id       id
     * @param response response
     * @return 是否成功
     */
    public boolean submitResponse(String id, T response) {
        return requestResponseHandler.submitResponse(id, response);
    }

    /**
     * 提交stream流的响应
     *
     * @param streamName streamName, 通常由symbol和WebSocketStreamType组合成
     * @param message    message
     */
    public abstract void submitStreamResponse(String streamName, T message);
}
