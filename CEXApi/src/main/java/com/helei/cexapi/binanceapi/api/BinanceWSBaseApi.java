


package com.helei.cexapi.binanceapi.api;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.BinanceWSApiClientClient;
import com.helei.cexapi.binanceapi.base.AbstractBinanceWSApi;
import com.helei.cexapi.binanceapi.constants.WebSocketCommandType;
import com.helei.cexapi.binanceapi.dto.WebSocketCommandBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;


@Slf4j
public class BinanceWSBaseApi extends AbstractBinanceWSApi {
    public BinanceWSBaseApi(BinanceWSApiClientClient binanceWSApiClient) throws URISyntaxException {
        super(binanceWSApiClient);
    }


    /**
     * 测试服务端联通性
     */
    public void pingServer() {
        binanceWSApiClient.sendPing();
    }


    /**
     * 查询服务端的时间
     * @param callback callback, 失败则会传入null. 由父类的线程池执行
     */
    public void queryServerTime(Consumer<Long> callback) {
        JSONObject command = WebSocketCommandBuilder.builder().setCommandType(WebSocketCommandType.TIME).build();

        String id = command.getString("id");
        binanceWSApiClient.sendRequest(1, id, command, result -> {
            if (result != null) {
                try {
                    callback.accept(result.getLong("serverTime"));
                    log.debug("get server time [{}], request id[{}] success", result, id);
                } catch (Exception e) {
                    callback.accept(null);
                    log.error("parse server time error, requestId [{}]", id,e);
                }
            } else {
                callback.accept(null);
                log.warn("get server time, request id[{}] fail", id);
            }
        });
    }


    /**
     * 查询交易规范信息,
     * @param permissions permissions
     * @param callback callback。失败则会传入null. 由父类的线程池执行
     */
    public void queryExchangeInfo(
            Consumer<JSONObject> callback,
            List<String> permissions
    ) {
        queryExchangeInfo(null, null, permissions, callback);
    }

    /**
     * 查询交易规范信息,
     * @param symbols symbols
     * @param callback callback。失败则会传入null. 由父类的线程池执行
     */
    public void queryExchangeInfo(
            List<String> symbols,
            Consumer<JSONObject> callback
    ) {
        queryExchangeInfo(null, symbols, null, callback);
    }

    /**
     * 查询交易规范信息, symbol,
     * @param symbol symbol
     * @param callback callback。失败则会传入null. 由父类的线程池执行
     */
    public void queryExchangeInfo(
            String symbol,
            Consumer<JSONObject> callback
    ) {
        queryExchangeInfo(symbol, null, null, callback);
    }


    /**
     * 查询交易规范信息, symbol,symbols,permissions三个参数只能生效一个，从前到后第一个不为空的生效
     * @param symbol symbol
     * @param symbols symbols
     * @param permissions permissions
     * @param callback callback。失败则会传入null. 由父类的线程池执行
     */
    public void queryExchangeInfo(
            String symbol,
            List<String> symbols,
            List<String> permissions,
            Consumer<JSONObject> callback
    ) {


        JSONObject jb = new JSONObject();
        if (StrUtil.isNotBlank(symbol)) {
            jb.put("symbol", symbol);
        }
        else if (symbols != null && !symbols.isEmpty()) {
            jb.put("symbols", symbols);
        }
        else if (permissions != null && !permissions.isEmpty()) {
            jb.put("permissions", permissions);
        }
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(WebSocketCommandType.EXCHANGE_INFO)
                .setParams(jb)
                .build();

        String id = command.getString("id");
        binanceWSApiClient.sendRequest(20, id, command, callback);
    }
}
