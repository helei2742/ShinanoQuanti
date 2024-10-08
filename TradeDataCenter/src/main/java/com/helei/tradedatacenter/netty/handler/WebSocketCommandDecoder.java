package com.helei.tradedatacenter.netty.handler;

import com.helei.tradedatacenter.entity.WebSocketCommand;
import com.helei.tradedatacenter.netty.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class WebSocketCommandDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() >= 0) {
            byte[] bytes = new byte[in.readableBytes()];
            in.readBytes(bytes);
            out.add(Serializer.Algorithm.Protostuff.deserialize(bytes, WebSocketCommand.class));
        }
    }
}
