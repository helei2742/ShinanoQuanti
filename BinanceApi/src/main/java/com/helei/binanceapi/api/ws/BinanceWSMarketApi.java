package com.helei.binanceapi.api.ws;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.AbstractBinanceWSApi;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.constants.trade.KLineInterval;
import com.helei.binanceapi.constants.command.MarketCommandType;
import com.helei.dto.ASKey;
import com.helei.dto.WebSocketCommandBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


/**
 * 现货api
 */
@Slf4j
public class BinanceWSMarketApi extends AbstractBinanceWSApi {


    public BinanceWSMarketApi(AbstractBinanceWSApiClient binanceWSApiClient) throws URISyntaxException {
        super(binanceWSApiClient);
    }

    /**
     * 获取当前深度信息。
     * 请注意，此请求返回有限的市场深度。
     * 如果需要持续监控深度信息更新，请考虑使用 WebSocket Streams：
     * <symbol>@depth<levels>
     * <symbol>@depth
     * 如果需要维护本地orderbook，您可以将 depth 请求与 <symbol>@depth streams 一起使用。
     * 权重(IP): 根据限制调整：
     * 限制	重量
     * 1–100	5
     * 101–500	25
     * 501–1000	50
     * 1001-5000	250
     * 参数:
     * 名称	类型	是否必需	描述
     * symbol	STRING	YES
     * limit	INT	NO	默认 100; 最大值 5000
     *
     * @param symbol   symbol
     * @param limit    limit
     * @param callback callback
     *                 {
     *                 "lastUpdateId": 2731179239,
     *                 // bid 水平从最高价到最低价排序。
     *                 "bids": [
     *                 [
     *                 "0.01379900",   // 价格
     *                 "3.43200000"    // 重量
     *                 ]
     *                 ],
     *                 // ask 水平从最低价到最高价排序。
     *                 "asks": [
     *                 [
     *                 "0.01380000",
     *                 "5.91700000"
     *                 ]
     *                 ]
     *                 }
     */
    public void queryDepth(String symbol, Integer limit, Consumer<JSONObject> callback) {
        int ipWeight = 100;
        if (limit >= 1 && limit <= 100) {
            ipWeight = 5;
        } else if (limit >= 101 && limit <= 500) {
            ipWeight = 25;
        } else if (limit >= 501 && limit <= 1000) {
            ipWeight = 50;
        } else if (limit >= 1001 && limit <= 5000) {
            ipWeight = 250;
        } else {
            ipWeight = 250;
            limit = 5000;
        }

        WebSocketCommandBuilder builder = WebSocketCommandBuilder
                .builder();
        builder.setCommandType(MarketCommandType.DEPTH);
        builder.addParam("symbol", symbol);
        builder.addParam("limit", limit);
        JSONObject command = builder
                .build();


        binanceWSApiClient.sendRequest(ipWeight, command, callback);
    }


    /**
     * 查询最近交易信息
     *
     * @param symbol   symbol
     * @param limit    多少条
     * @param callback callback
     *                 [
     *                 {
     *                 "id": 194686783,
     *                 "price": "0.01361000",
     *                 "qty": "0.01400000",
     *                 "quoteQty": "0.00019054",
     *                 "time": 1660009530807,
     *                 "isBuyerMaker": true,
     *                 "isBestMatch": true
     *                 }
     *                 ]
     */
    public void queryTradesRecent(String symbol, int limit, Consumer<JSONObject> callback) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(MarketCommandType.TRADES_RECENT)
                .addParam("symbol", symbol)
                .addParam("limit", limit)
                .build();

        binanceWSApiClient.sendRequest(20, command, callback);
    }


    /**
     * 查询历史k线数据
     *
     * @param symbol          symbol
     * @param interval        interval
     * @param startTimeSecond startTimeSecond
     * @param limit           limit
     * @return result
     */
    public CompletableFuture<JSONObject> queryHistoryKLine(
            String symbol,
            KLineInterval interval,
            long startTimeSecond,
            int limit
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(MarketCommandType.KLINES)
                .addParam("symbol", symbol.toLowerCase())
                .addParam("interval", interval.getDescribe())
                .addParam("startTime", startTimeSecond * 1000)
                .addParam("limit", limit)
                .build();

        return binanceWSApiClient.sendRequest(2, command, ASKey.EMPTY_ASKEY);
    }
}

