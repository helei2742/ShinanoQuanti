package com.helei.tradedatacenter.netty.base;

import com.helei.tradedatacenter.subscribe.binanceapi.dto.WebSocketCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * 规定里netty handler 具有的行为
 */
public interface NettyBaseHandler {

   /**
    * 发送消息
    * @param context context
    * @param webSocketCommand 消息体
    */
   void sendMsg(ChannelHandlerContext context, WebSocketCommand webSocketCommand);

   /**
    * 打印日志
    * @param logStr 日志字符串
    */
   void printLog(String logStr);
}
