package com.helei.binanceapi.base;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.helei.netty.base.AbstractWebSocketClientHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AbstractBinanceWSApiClientHandler extends AbstractWebSocketClientHandler<JSONObject, JSONObject> {


    @Override
    protected void whenReceiveMessage(String text) {
        JSONObject message = JSONObject.parseObject(text);

        String id = message.getString("id");
        String stream = message.getString("stream");

        if (StrUtil.isNotBlank(id)) {
            //有id，是发送请求的响应
            //提交response

            websocketClient.submitResponse(id, message);
        } else if (StrUtil.isNotBlank(stream)) {
            //没有id，是服务端推送的消息
            log.debug("stream name[{}] get stream response [{}]", stream, message);
            websocketClient.submitStreamResponse(stream, message);
        }
    }

}
