package com.helei.cexapi.binanceapi.api.ws;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.BinanceWSApiClient;
import com.helei.cexapi.binanceapi.base.AbstractBinanceWSApi;
import com.helei.cexapi.binanceapi.constants.command.AccountCommandType;
import com.helei.cexapi.binanceapi.dto.ASKey;
import com.helei.cexapi.binanceapi.dto.WebSocketCommandBuilder;
import lombok.extern.slf4j.Slf4j;

import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;


/**
 * 合约账户api
 */
@Slf4j
public class BinanceWSContractAccountApi extends AbstractBinanceWSApi {

    public BinanceWSContractAccountApi(BinanceWSApiClient binanceWSApiClient) throws URISyntaxException {
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
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(AccountCommandType.ACCOUNT_STATUS)
                .build();

        log.info("查询账户信息，请求[{}]", command);
        return binanceWSApiClient.sendRequest(5, command, asKey);
    }

}
