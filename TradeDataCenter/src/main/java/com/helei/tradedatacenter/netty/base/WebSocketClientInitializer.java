package com.helei.tradedatacenter.netty.base;

import com.helei.tradedatacenter.netty.WebSocketClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.ssl.SslContext;

public class WebSocketClientInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;
    private final WebSocketClientHandler handler;

    public WebSocketClientInitializer(SslContext sslCtx, WebSocketClientHandler handler) {
        this.sslCtx = sslCtx;
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (sslCtx != null) {
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        }
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(8192));
        pipeline.addLast(new WebSocketClientProtocolHandler(handler.getHandshaker()));
        pipeline.addLast(handler);
    }
}
