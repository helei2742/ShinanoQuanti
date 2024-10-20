package com.helei.binanceapi.api.ws;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSApiClient;
import com.helei.binanceapi.base.AbstractBinanceWSApi;
import com.helei.binanceapi.constants.command.AccountCommandType;
import com.helei.binanceapi.dto.ASKey;
import com.helei.dto.WebSocketCommandBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;


/**
 * 用户相关api
 */
@Slf4j
public class BinanceWSSpotAccountApi extends AbstractBinanceWSApi {

    public BinanceWSSpotAccountApi(BinanceWSApiClient binanceWSApiClient) throws URISyntaxException {
        super(binanceWSApiClient);
    }

    /**
     * 账户信息
     * @param omitZeroBalances 是否显示非0余额
     * @param asKey 签名参数
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> accountStatus(
            Boolean omitZeroBalances,
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .addParam("omitZeroBalances", omitZeroBalances)
                .setCommandType(AccountCommandType.ACCOUNT_STATUS)
                .build();

        log.info("query account status command [{}]", command);
        return binanceWSApiClient.sendRequest(20, command, asKey);
    }

    /**
     * 显示用户在所有时间间隔内的未成交订单计数。
     * @param asKey 签名参数
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> accountRateLimitsOrders(ASKey asKey) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(AccountCommandType.ACCOUNT_RATE_LIMITS_ORDERS)
                .build();

        log.info("query account rate limit orders command [{}]", command);
        return binanceWSApiClient.sendRequest(40, command, asKey);
    }

    /**
     * 账户订单历史
     * 获取所有账户订单； 有效，已取消或已完成。按时间范围过滤。
     * 订单状态报告与 order.status 相同。
     * 请注意，某些字段是可选的，仅在订单中有设置它们时才包括。
     * --
     * 如果指定了 startTime 和/或 endTime，则忽略 orderId。
     * 订单是按照最后一次更新的执行状态的time过滤的。
     * 如果指定了 orderId，返回的订单将是订单ID >= orderId
     * 如果不指定条件，则返回最近的订单。
     * 对于某些历史订单，cummulativeQuoteQty 响应字段可能为负数，代表着此时数据还不可用。
     * @param symbol 必须
     * @param orderId 起始订单ID，可选
     * @param startTime 可选
     * @param endTime 可选
     * @param limit 可选
     * @param asKey 签名参数
     * @return  CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> accountAllOrders(
            String symbol,
            Integer orderId,
            Long startTime,
            Long endTime,
            Integer limit,
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .addParam("symbol", symbol)
                .addParam("orderId", orderId)
                .addParam("startTime", startTime)
                .addParam("endTime", endTime)
                .addParam("limit", limit)
                .setCommandType(AccountCommandType.ACCOUNT_ALL_ORDERS)
                .build();

        log.info("query account [{}] orders", symbol);
        return binanceWSApiClient.sendRequest(20, command, asKey);
    }


    /**
     * 账户成交历史
     * 如果指定了 fromId，则返回的交易将是 交易ID >= fromId。
     * 如果指定了 startTime 和/或 endTime，则交易按执行时间（time）过滤。
     * fromId 不能与 startTime 和 endTime 一起使用。
     * 如果指定了 orderId，则只返回与该订单相关的交易。
     * startTime 和 endTime 不能与 orderId 一起使用。
     * 如果不指定条件，则返回最近的交易。
     * @param symbol 必须
     * @param orderId 起始订单ID，可选
     * @param startTime 可选
     * @param endTime 可选
     * @param fromId 起始交易 ID
     * @param limit 可选
     * @param asKey 签名参数
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> accountTrades(
            String symbol,
            Integer orderId,
            Long startTime,
            Long endTime,
            Integer fromId,
            Integer limit,
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .addParam("symbol", symbol)
                .addParam("orderId", orderId)
                .addParam("startTime", startTime)
                .addParam("endTime", endTime)
                .addParam("fromId", fromId)
                .addParam("limit", limit)
                .setCommandType(AccountCommandType.ACCOUNT_ALL_ORDERS)
                .build();

        log.info("query account all trades, command[{}]", command);
        return binanceWSApiClient.sendRequest(20, command, asKey);
    }
}
