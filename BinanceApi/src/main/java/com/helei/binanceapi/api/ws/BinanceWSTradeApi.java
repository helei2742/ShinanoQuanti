
package com.helei.binanceapi.api.ws;

        import cn.hutool.core.util.StrUtil;
        import com.alibaba.fastjson.JSON;
        import com.alibaba.fastjson.JSONObject;
        import com.helei.binanceapi.BinanceWSApiClient;
        import com.helei.binanceapi.base.AbstractBinanceWSApi;
        import com.helei.binanceapi.constants.CancelRestrictions;
        import com.helei.binanceapi.dto.order.BaseOrder;
        import com.helei.binanceapi.constants.command.TradeCommandType;
        import com.helei.binanceapi.dto.ASKey;
        import com.helei.dto.WebSocketCommandBuilder;
        import lombok.extern.slf4j.Slf4j;

        import java.net.URISyntaxException;
        import java.util.concurrent.CompletableFuture;


/**
 * 交易相关api
 */
@Slf4j
public class BinanceWSTradeApi extends AbstractBinanceWSApi {

    public BinanceWSTradeApi(BinanceWSApiClient binanceWSApiClient) throws URISyntaxException {
        super(binanceWSApiClient);
    }


    /**
     * 提交订单，
     * @param order 订单信息
     * @param asKey 签名需要的参数
     * @return 下单的的结果CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> commitOrder(BaseOrder order, ASKey asKey) {
        String jsonString = JSON.toJSONString(order);
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(TradeCommandType.ORDER_PLACE)
                .setParams(JSON.parseObject(jsonString))
                .build();

        log.info("commit order command [{}]", jsonString);
        return binanceWSApiClient.sendRequest(1, command, asKey);
    }

    /**
     * 提交测试订单，
     * @param order 订单信息
     * @param asKey 签名需要的参数
     * @return 下单的的结果CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> commitTestOrder(BaseOrder order, ASKey asKey) {
        String jsonString = JSON.toJSONString(order);
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .setCommandType(TradeCommandType.ORDER_TEST)
                .setParams(JSON.parseObject(jsonString))
                .build();

        log.info("commit test order command [{}]", jsonString);
        return binanceWSApiClient.sendRequest(1, command, asKey);
    }

    /**
     * 查询订单
     * 如果同时指定了 orderId 和 origClientOrderId 参数，仅使用 orderId 并忽略 origClientOrderId。
     * 对于某些历史订单，cummulativeQuoteQty 响应字段可能为负数，意味着此时数据不可用。
     * @param symbol symbol
     * @param orderId orderId
     * @param origClientOrderId origClientOrderId
     * @param asKey 签名需要的参数
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> queryOrder(
            String symbol,
            Integer orderId,
            String origClientOrderId,
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .addParam("symbol", symbol)
                .addParam("orderId", orderId)
                .addParam("origClientOrderId", origClientOrderId)
                .setCommandType(TradeCommandType.ORDER_STATUS)
                .build();

        log.info("query order [{}]", command);
        return binanceWSApiClient.sendRequest(4, command, asKey);
    }


    /**
     * 撤销订单
     * 如果同时指定了 orderId 和 origClientOrderId 参数，仅使用 orderId 并忽略 origClientOrderId。
     * newClientOrderId 将替换已取消订单的 clientOrderId，为新订单腾出空间。
     * 如果您取消属于订单列表的订单，则整个订单列表将被取消。
     * @param symbol symbol
     * @param orderId 按 orderId 取消订单
     * @param origClientOrderId 按 clientOrderId 取消订单
     * @param newClientOrderId 已取消订单的新 ID。如果未发送，则自动生成
     * @param cancelRestrictions 支持的值:
     *                            ONLY_NEW - 如果订单状态为 NEW，撤销将成功。
     *                            ONLY_PARTIALLY_FILLED - 如果订单状态为 PARTIALLY_FILLED，撤销将成功。
     * @param asKey 签名需要的参数
     * @return  CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> cancelOrder(
            String symbol,
            Integer orderId,
            String origClientOrderId,
            String newClientOrderId,
            CancelRestrictions cancelRestrictions,
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .addParam("symbol", symbol)
                .addParam("orderId", orderId)
                .addParam("origClientOrderId", origClientOrderId)
                .addParam("newClientOrderId", newClientOrderId)
                .addParam("cancelRestrictions", cancelRestrictions)
                .setCommandType(TradeCommandType.ORDER_CANCEL)
                .build();

        log.info("cancel order [{}]", command);
        return binanceWSApiClient.sendRequest(1, command, asKey);
    }

    /**
     * 撤销单一交易对的所有挂单
     * @param symbol symbol
     * @param asKey 签名需要的参数
     * @return  CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> openOrdersCancelAll(
            String symbol,
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .addParam("symbol", symbol)
                .setCommandType(TradeCommandType.OPEN_ORDER_CANCEL_ALL)
                .build();

        log.info("cancel all order [{}]", command);
        return binanceWSApiClient.sendRequest(1, command, asKey);
    }



    /**
     * 查询用户当前挂单情况
     * @param symbol symbol
     * @param asKey 签名需要的参数
     * @return CompletableFuture<JSONObject>
     */
    public CompletableFuture<JSONObject> openOrdersStatus(
            String symbol,
            ASKey asKey
    ) {
        JSONObject command = WebSocketCommandBuilder
                .builder()
                .addParam("symbol", symbol)
                .setCommandType(TradeCommandType.OPEN_ORDER_STATUS)
                .build();
        int weight = 6;
        if (StrUtil.isBlank(symbol)) {
            weight = 80;
        }

        log.info("open order status [{}]", command);
        return binanceWSApiClient.sendRequest(weight, command, asKey);
    }
}

