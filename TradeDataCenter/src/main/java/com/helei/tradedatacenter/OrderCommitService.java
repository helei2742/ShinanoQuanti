package com.helei.tradedatacenter;

import com.alibaba.fastjson.JSONObject;
import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
import com.helei.tradedatacenter.config.TradeConfig;
import com.helei.tradedatacenter.conventor.AccountInfoMapper;
import com.helei.tradedatacenter.dto.AccountInfo;
import com.helei.tradedatacenter.dto.AccountLocationConfig;
import com.helei.tradedatacenter.dto.OriginOrder;
import com.helei.tradedatacenter.support.OrderBuildSupporter;
import com.helei.tradedatacenter.util.CalculatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.concurrent.CompletableFuture;

/**
 * 订单提交服务
 */
@Slf4j
public class OrderCommitService {

    private final AccountInfoService accountInfoService;
    private final OrderBuildSupporter orderBuildSupporter;

    public OrderCommitService(AccountInfoService accountInfoService, OrderBuildSupporter orderBuildSupporter) {
        this.accountInfoService = accountInfoService;
        this.orderBuildSupporter = orderBuildSupporter;
    }

    public DataStream<BaseOrder> commitOrder(DataStream<OriginOrder> originOrderStream) {
        return originOrderStream.process(new OriginOrderStreamProcessor(accountInfoService, orderBuildSupporter));
    }


    static class OriginOrderStreamProcessor extends ProcessFunction<OriginOrder, BaseOrder> {
        private static AccountInfoService accountInfoService;

        private static  OrderBuildSupporter orderBuildSupporter;

        OriginOrderStreamProcessor(AccountInfoService accountInfoService, OrderBuildSupporter orderBuildSupporter) {
            this.accountInfoService = accountInfoService;
            this.orderBuildSupporter = orderBuildSupporter;
        }

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

                    String uid = accountInfo.getUId();

                    Double freeUsdt = accountInfo.getFreeUsdt();
                    if (freeUsdt < TradeConfig.MIN_ORDER_USDT_COUNT_LIMIT) {
                        log.warn("账户[{}]的剩余USDT[{}]小于限制[{}]，取消下单", uid, freeUsdt, TradeConfig.MIN_ORDER_USDT_COUNT_LIMIT);
                        return;
                    }

                    //1，获取仓位设置
                    AccountLocationConfig locationConfig = accountInfoService.getAccountLocationConfig(uid);
                    //2.计算仓位大小
                    double positionSize = CalculatorUtil.calculatePositionSize(freeUsdt, locationConfig, originOrder.getTargetPrice().doubleValue());

                    //3.构建订单
                    BaseOrder order = orderBuildSupporter.buildOrder(originOrder, positionSize);
                    //4。发送
                    collector.collect(order);
                });
            }
        }
    }
}
