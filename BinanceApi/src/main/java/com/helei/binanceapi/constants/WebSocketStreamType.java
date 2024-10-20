package com.helei.binanceapi.constants;


import com.helei.constants.WebSocketStreamParamKey;
import lombok.Getter;
import java.util.Map;


@Getter
public enum WebSocketStreamType {

    /**
     * 交易流归集，推送交易信息，是对单一订单的集合
     */
    AGG_TRADE("aggTrade", (symbol, params) -> symbol + "@aggTrade"),

    /**
     * K线stream逐秒推送所请求的K线种类(最新一根K线)的更新。此更新是基于 UTC+0 时区的。
     * Stream 名称: <symbol>@kline_<interval
     */
    KLINE("kline", (symbol, params) -> symbol + "@kline_" + params.get(WebSocketStreamParamKey.KLINE_INTERVAL)),

    /**
     * K线stream逐秒推送所请求的K线种类(最新一根K线)的更新。此更新是基于 UTC+8 时区的。
     * UTC+8 时区偏移量：
     * K线间隔的开始和结束时间会基于 UTC+8 时区。例如， 1d K线将在 UTC+8 当天开始，并在 UTC+8 当日完结时随之结束。
     * 请注意，Payload中的 E（event time），t（start time）和 T（close time）是 Unix 时间戳，它们始终以 UTC 格式解释。
     * Stream 名称: <symbol>@kline_<interval>@+08:00
     */
    MOVE_KLINE("kline", (symbol, params) -> symbol + "@kline_" + params.get(WebSocketStreamParamKey.KLINE_INTERVAL) + "@" + params.get(WebSocketStreamParamKey.KLINE_TIMEZONE)),

    /**
     * K线stream逐秒推送所请求的K线种类(最新一根K线)的更新。
     *
     * 合约类型:
     * perpetual 永续合约
     * current_quarter 当季交割合约
     * next_quarter 次季交割合约
     * 订阅Kline需要提供间隔参数,最短为分钟线,最长为月线。支持以下间隔:
     *
     * m -> 分钟; h -> 小时; d -> 天; w -> 周; M -> 月
     */
    CONTINUOUS_KLINE("continuousKline", (symbol, params) ->
            String.format("%s_%s@continuousKline_%s", symbol, params.get(WebSocketStreamParamKey.CONTRACT_TYPE), params.get(WebSocketStreamParamKey.KLINE_INTERVAL))),

    /**
     * 按Symbol刷新的最近24小时精简ticker信息
     * Stream 名称: <symbol>@miniTicker
     */
    MINI_TICKER("miniTicker", (symbol, params) -> symbol + "@miniTicker"),

    /**
     * 按Symbol刷新的24小时完整ticker信息
     */
    TICKER("ticker",  (symbol, params) -> symbol + "@ticker"),

    /**
     * 所有symbol 24小时完整ticker信息.需要注意的是，只有发生变化的ticker更新才会被推送。
     */
    ALL_TICKER("!ticker@arr", (symbol, params) -> "@!ticker@arr"),

    /**
     * 按symbol的最优挂单信息
     */
    BOOK_TICKER("bookTicker", ((symbol, params) -> "@bookTicker")),

    /**
     * 强平订单，推送特定symbol的强平订单快照信息。 1000ms内至多仅推送一条最近的强平订单作为快照
     */
    FORCE_ORDER("forceOrder", ((symbol, params) -> symbol + "@forceOrder")),

    /**
     * 所有symbol 24小时完整ticker信息.需要注意的是，只有发生变化的ticker更新才会被推送。
     */
    ALL_MINI_TICKER("!miniTicker@arr", (symbol, params) -> "!miniTicker@arr"),

    /**
     * 最新标记价格
     */
    MARK_PRICE("markPrice", (symbol, params) -> symbol + "@markPrice"),

    /**
     * 全市场最新标记价格
     */
    ALL_MARK_PRICE("!markPrice@arr", (symbol, params) -> "!markPrice@arr")
    ;


    WebSocketStreamType(String description, AbstractBinanceWSSHandler handler) {
        this.description = description;
        this.handler = handler;
    }

    private final String description;

    private final AbstractBinanceWSSHandler handler;


    public interface AbstractBinanceWSSHandler {
        String buildStreamName(String symbol, Map<String, Object> params);
    }

    @Override
    public String toString() {
        return description;
    }
}
