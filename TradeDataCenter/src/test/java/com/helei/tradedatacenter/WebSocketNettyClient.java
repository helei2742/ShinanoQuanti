package com.helei.tradedatacenter;

import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

@Slf4j
public class WebSocketNettyClient {

    public static void main(String[] args)  {

        EventLoopGroup group = new NioEventLoopGroup();
        final ClientHandler handler =new ClientHandler();
        try {
            URI websocketURI = new URI("wss://dstream.binance.com");
            //进行握手
            log.debug("握手开始");
            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(websocketURI, WebSocketVersion.V13, (String)null, false,new DefaultHttpHeaders());
            System.out.println(websocketURI.getScheme());
            System.out.println(websocketURI.getHost());
            System.out.println(websocketURI.getPort());
            SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();



            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            //放到第一位 addFirst 支持wss链接服务端
                            ch.pipeline().addFirst(sslCtx.newHandler(ch.alloc(), websocketURI.getHost(),websocketURI.getPort()));

                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加一个http的编解码器
                            pipeline.addLast(new HttpClientCodec());
                            // 添加一个用于支持大数据流的支持
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 添加一个聚合器，这个聚合器主要是将HttpMessage聚合成FullHttpRequest/Response
                            pipeline.addLast(new HttpObjectAggregator(1024 * 64));
                            pipeline.addLast(handler);
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536 * 10));

                        }
                    });



            final Channel channel=bootstrap.connect(websocketURI.getHost(),433).sync().channel();
            handler.setHandshaker(handshaker);
            handshaker.handshake(channel);
            //发送消息
            System.out.println("发送消息");
            JSONObject hhh = new JSONObject();
            hhh.put("cmd","test");
            for (int i = 0; i < 5; i++) {
                Thread.sleep(2000);
                channel.writeAndFlush(new TextWebSocketFrame(hhh.toString()));
            }
            //阻塞等待是否握手成功
            handler.handshakeFuture().sync();
            System.out.println("握手成功");

            //发送消息
            System.out.println("发送消息");
            JSONObject clientJson = new JSONObject();
            clientJson.put("cmd","test");
            channel.writeAndFlush(new TextWebSocketFrame(clientJson.toString()));

            // 等待连接被关闭
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            group.shutdownGracefully();
        }

    }

}
