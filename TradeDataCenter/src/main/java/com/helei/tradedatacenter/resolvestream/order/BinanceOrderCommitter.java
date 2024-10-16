package com.helei.tradedatacenter.resolvestream.order;

        import com.alibaba.fastjson.JSONObject;
        import com.helei.cexapi.binanceapi.BinanceWSApiClient;
        import com.helei.cexapi.binanceapi.api.BinanceWSTradeApi;
        import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
        import com.helei.tradedatacenter.dto.OriginOrder;
        import lombok.extern.slf4j.Slf4j;

        import java.util.concurrent.ExecutionException;


/**
 * 币安订单提交
 */
@Slf4j
public class BinanceOrderCommitter extends AbstractOrderCommitter {

    private final BinanceWSTradeApi tradeApi;

    public BinanceOrderCommitter(BinanceWSApiClient binanceWSApiClient) {
        tradeApi = binanceWSApiClient.getTradeApi();
    }


    @Override
    public boolean commitTradeOrder(OriginOrder order) {
        JSONObject response = null;
        try {
            //TODO 用户
            response = tradeApi.commitOrder(null, null).get();

            if (response == null) {
                log.error("get trade response is null");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("get trade response error", e);
        }


        return false;
    }
}