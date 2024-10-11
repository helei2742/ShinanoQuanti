

package com.helei.cexapi.binanceapi.api;

import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.binanceapi.base.AbstractBinanceWSApi;
import com.helei.cexapi.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.cexapi.binanceapi.constants.WebSocketStreamType;
import com.helei.cexapi.binanceapi.dto.StreamSubscribeEntity;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 币按stream api
 */
public class BinanceWSStreamApi extends AbstractBinanceWSApi {

    public BinanceWSStreamApi(BinanceWSApiClientClient binanceWSApiClient) throws URISyntaxException {
        super(binanceWSApiClient);
    }

    public StreamCommandBuilder builder() {
        return new StreamCommandBuilder(binanceWSApiClient);
    }



    public static class StreamCommandBuilder{
        private final BinanceWSApiClientClient binanceWSApiClient;

        private String symbol = null;

        private List<StreamSubscribeEntity> subscribeList = null;

        StreamCommandBuilder(BinanceWSApiClientClient binanceWSApiClient){
            this.binanceWSApiClient = binanceWSApiClient;
        }

        /**
         * 设置symbol
         * @param symbol symbol
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }


        /**
         * 添加订阅类型， 必须在设置symbol之后
         * @param subscribeType subscribeType
         * @param invocationHandler invocationHandler
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder addSubscribeEntity(
                WebSocketStreamType subscribeType,
                SubscribeResultInvocationHandler invocationHandler
        ) {
            return addSubscribeEntity(subscribeType, invocationHandler, null);
        }

        /**
         * 添加订阅类型， 必须在设置symbol之后
         * @param subscribeType subscribeType
         * @param invocationHandler invocationHandler
         * @param params params
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder addSubscribeEntity(
                WebSocketStreamType subscribeType,
                SubscribeResultInvocationHandler invocationHandler,
                Map<String, Object> params
        ) {
            return addSubscribeEntity(subscribeType, invocationHandler, null, params);
        }

        /**
         * 添加订阅类型， 必须在设置symbol之后
         * @param subscribeType subscribeType
         * @param invocationHandler invocationHandler
         * @param executorService 执行的线程池，如果设置了，在收到订阅的消息的时候会优先使用该线程池
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder addSubscribeEntity(
                WebSocketStreamType subscribeType,
                SubscribeResultInvocationHandler invocationHandler,
                ExecutorService executorService,
                Map<String, Object> params
        ) {
            return addSubscribeEntity(subscribeType, invocationHandler, executorService, false, params);
        }

        /**
         * 添加订阅类型， 必须在设置symbol之后
         * @param subscribeType subscribeType
         * @param invocationHandler invocationHandler
         * @param executorService 执行的线程池，如果设置了，在收到订阅的消息的时候会优先使用该线程池
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder addSubscribeEntity(
                WebSocketStreamType subscribeType,
                SubscribeResultInvocationHandler invocationHandler,
                ExecutorService executorService,
                boolean signature,
                Map<String, Object> params
        ) {
            return addSubscribeEntity(new StreamSubscribeEntity(symbol, subscribeType, invocationHandler, executorService, params, signature));
        }
        /**
         * 添加订阅类型， 必须在设置symbol之后
         * @param subscribeEntity subscribeEntity
         * @return StreamCommandBuilder
         */
        public synchronized StreamCommandBuilder addSubscribeEntity(
                StreamSubscribeEntity subscribeEntity
        ) {
            if (subscribeList == null) {
                subscribeList = new ArrayList<>();
            }

            subscribeList.add(subscribeEntity);
            return this;
        }

        public void subscribe() {
            binanceWSApiClient.subscribeStream(symbol, subscribeList);
        }
    }

}

