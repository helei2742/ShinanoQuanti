package com.helei.binanceapi.api.ws;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.base.AbstractBinanceWSApi;
import com.helei.binanceapi.base.AbstractBinanceWSApiClient;
import com.helei.binanceapi.constants.command.AccountCommandType;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.WebSocketCommandBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class BinanceWSAccountApi extends AbstractBinanceWSApi {


    public BinanceWSAccountApi(AbstractBinanceWSApiClient binanceWSApiClient) throws URISyntaxException {
        super(binanceWSApiClient);
    }


    /**
     * 账户余额 , 查询账户余额
     * @param asKey 签名参数
     * @return CompletableFuture<JSONObject>、
     * {
     *     "id": "605a6d20-6588-4cb9-afa0-b0ab087507ba",
     *     "status": 200,
     *     "result": {
     *         [
     *            {
     *              "accountAlias": "SgsR",              // 账户唯一识别码
     *              "asset": "USDT",  	                // 资产
     *              "balance": "122607.35137903",        // 总余额
     *              "crossWalletBalance": "23.72469206", // 全仓余额
     *              "crossUnPnl": "0.00000000"           // 全仓持仓未实现盈亏
     *              "availableBalance": "23.72469206",   // 下单可用余额
     *              "maxWithdrawAmount": "23.72469206",  // 最大可转出余额
     *              "marginAvailable": true,            // 是否可用作联合保证金
     *             "updateTime": 1617939110373
     *             }
     *         ]
     *     },
     *     "rateLimits": [
     *       {
     *         "rateLimitType": "REQUEST_WEIGHT",
     *         "interval": "MINUTE",
     *         "intervalNum": 1,
     *         "limit": 2400,
     *         "count": 20
     *       }
     *     ]
     * }
     */
    public CompletableFuture<JSONObject> accountBalance(
            ASKey asKey
    ) {

        justContract();

        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(AccountCommandType.ACCOUNT_BALANCE)
                .build();

        log.info("查询账户余额，请求[{}]", command);
        return binanceWSApiClient.sendRequest(5, command, asKey);
    }


    /**
     * 账户信息
     * @param asKey 签名参数
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> accountStatus(
            ASKey asKey,
            Boolean omitZeroBalances
    ) {
        WebSocketCommandBuilder builder = WebSocketCommandBuilder
                .builder()
                .addParam("omitZeroBalances", omitZeroBalances);
        if (omitZeroBalances && binanceWSApiClient.getTradeType().equals(TradeType.CONTRACT)) {
            builder.setCommandType(AccountCommandType.ACCOUNT_STATUS_V2);
        } else {
            builder.setCommandType(AccountCommandType.ACCOUNT_STATUS);
        }

        JSONObject command = builder.build();

        log.info("查询账户信息，请求[{}]", command);
        return binanceWSApiClient.sendRequest(5, command, asKey);
    }

    /**
     * 显示用户在所有时间间隔内的未成交订单计数。
     * @param asKey 签名参数
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> accountRateLimitsOrders(ASKey asKey) {

        justSpot();

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
        justSpot();

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
        justSpot();

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


    /**
     * 只允许现货环境请求
     */
    private void justSpot() {
        TradeType tradeType = binanceWSApiClient.getTradeType();
        if (!tradeType.equals(TradeType.SPOT)) {
            throw new RuntimeException(String.format("账户余额接口只允许[%s]类型请求", TradeType.SPOT));
        }
    }

    /**
     * 只允许合约环境请求
     */
    private void justContract() {
        TradeType tradeType = binanceWSApiClient.getTradeType();
        if (!tradeType.equals(TradeType.CONTRACT)) {
            throw new RuntimeException(String.format("账户余额接口只允许[%s]类型请求", TradeType.CONTRACT));
        }
    }
}
