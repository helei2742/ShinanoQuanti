package com.helei.binanceapi.api.ws;

import com.helei.binanceapi.base.AbstractBinanceWSApi;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.base.SubscribeResultInvocationHandler;
import com.helei.binanceapi.constants.WebSocketStreamType;
import com.helei.dto.ASKey;
import com.helei.binanceapi.dto.StreamSubscribeEntity;

import java.util.concurrent.ExecutorService;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 币按stream api
 */
public class BinanceWSStreamApi extends AbstractBinanceWSApi {

    public BinanceWSStreamApi(AbstractBinanceWSApiClient binanceWSApiClient) throws URISyntaxException {
        super(binanceWSApiClient);
    }

    public StreamCommandBuilder builder() {
        return new StreamCommandBuilder(binanceWSApiClient);
    }


    public static class StreamCommandBuilder {
        private final AbstractBinanceWSApiClient binanceWSApiClient;

        private String symbol = null;

        private List<StreamSubscribeEntity> subscribeList = null;

        StreamCommandBuilder(AbstractBinanceWSApiClient binanceWSApiClient) {
            this.binanceWSApiClient = binanceWSApiClient;
        }

        /**
         * 设置symbol
         *
         * @param symbol symbol
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }


        /**
         * 添加订阅类型， 必须在设置symbol之后
         *
         * @param subscribeType     subscribeType
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
         *
         * @param subscribeType     subscribeType
         * @param invocationHandler invocationHandler
         * @param params            params
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
         *
         * @param subscribeType     subscribeType
         * @param invocationHandler invocationHandler
         * @param executorService   执行的线程池，如果设置了，在收到订阅的消息的时候会优先使用该线程池
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder addSubscribeEntity(
                WebSocketStreamType subscribeType,
                SubscribeResultInvocationHandler invocationHandler,
                ExecutorService executorService,
                Map<String, Object> params
        ) {
            return addSubscribeEntity(subscribeType, invocationHandler, executorService, null, params);
        }

        /**
         * 添加订阅类型， 必须在设置symbol之后
         *
         * @param subscribeType     subscribeType
         * @param invocationHandler invocationHandler
         * @param executorService   执行的线程池，如果设置了，在收到订阅的消息的时候会优先使用该线程池
         * @return StreamCommandBuilder
         */
        public StreamCommandBuilder addSubscribeEntity(
                WebSocketStreamType subscribeType,
                SubscribeResultInvocationHandler invocationHandler,
                ExecutorService executorService,
                ASKey asKey,
                Map<String, Object> params
        ) {
            return addSubscribeEntity(new StreamSubscribeEntity(symbol, subscribeType, invocationHandler, executorService, params, asKey));
        }

        /**
         * 添加订阅类型， 必须在设置symbol之后
         *
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
            binanceWSApiClient.subscribeStream(subscribeList);
        }
    }

}
