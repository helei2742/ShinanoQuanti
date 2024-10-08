package com.helei.tradedatacenter.netty.handler;

import com.helei.tradedatacenter.entity.WebSocketCommand;
import com.helei.tradedatacenter.netty.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class WebSocketCommandEncoder extends MessageToByteEncoder<WebSocketCommand> {



    @Override
    protected void encode(ChannelHandlerContext ctx, WebSocketCommand webSocketCommand, ByteBuf out)throws Exception{
        out.writeBytes(Serializer.Algorithm.Protostuff.serialize(webSocketCommand));
    }
}
