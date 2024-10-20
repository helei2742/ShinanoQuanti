
package com.helei.binanceapi.dto;

import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.binanceapi.constants.WebSocketStreamType;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;


/**
 * 流订阅的实体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class StreamSubscribeEntity {

    /**
     * 币种
     */
    private String symbol;

    /**
     * 流订阅的类型
     */
    private WebSocketStreamType subscribeType;

    /**
     * 订阅结果回调
     */
    private SubscribeResultInvocationHandler invocationHandler;

    /**
     * 执行回调的线程池
     */
    private ExecutorService callbackExecutor;

    /**
     * 参数
     */
    private Map<String, Object> params;

    private ASKey asKey;


    /**
     * 添加参数
     * @param key key
     * @param value value
     * @return StreamSubscribeEntity
     */
    public synchronized StreamSubscribeEntity addParam(String key, java.lang.Object value) {
        if (params == null) params = new HashMap<>();
        params.put(key, value);
        return this;
    }

    /**
     * 生成参数行,也就是stream name
     * @return 参数行
     */
    public String getStreamName() {
        return subscribeType.getHandler().buildStreamName(symbol, params);
    }

}
