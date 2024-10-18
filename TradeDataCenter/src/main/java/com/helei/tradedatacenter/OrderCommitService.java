package com.helei.tradedatacenter;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.tradedatacenter.config.TradeConfig;
import com.helei.tradedatacenter.conventor.AccountInfoMapper;
import com.helei.tradedatacenter.dto.AccountInfo;
import com.helei.tradedatacenter.dto.AccountLocationConfig;
import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.util.CalculatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 订单提交服务
 */
@Slf4j
public class OrderCommitService {



    public DataStream<BaseOrder> commitOrder(DataStream<OriginOrder> originOrderStream) {
        return originOrderStream.process(new ProcessFunction<OriginOrder, BaseOrder>() {
            private transient AccountInfoService accountInfoService;

            @Override
            public void open(Configuration parameters) throws Exception {
                accountInfoService.init();
            }

            @Override
            public void processElement(OriginOrder originOrder, ProcessFunction<OriginOrder, BaseOrder>.Context context, Collector<BaseOrder> collector) throws Exception {

                for (CompletableFuture<JSONObject> future : accountInfoService.getNewestSubscribedAccount(originOrder.getSymbol())) {
                    future.thenAcceptAsync((accountInfoJSON) -> {
                        AccountInfo accountInfo = AccountInfoMapper.mapJsonToAccountInfo(accountInfoJSON);
                        log.info("获取到最新的账户信息[{}]", accountInfo);
                        String uId = accountInfo.getUId();
                        Double freeUsdt = accountInfo.getFreeUsdt();

                        if (freeUsdt < TradeConfig.MIN_ORDER_USDT_COUNT_LIMIT) {
                            log.warn("账户[{}]的剩余USDT[{}]小于限制[{}]，取消下单", uId, freeUsdt, TradeConfig.MIN_ORDER_USDT_COUNT_LIMIT);
                            return;
                        }

                        AccountLocationConfig locationConfig = accountInfoService.getAccountLocationConfig(uId);
                        //计算可开数量
                        CalculatorUtil.calculatePositionSize(freeUsdt, locationConfig, originOrder.getTargetPrice())

                    });
                }
            }
        });
    }
}
