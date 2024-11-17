package com.helei.realtimedatacenter.controller;


import com.helei.constants.CEXType;
import com.helei.dto.base.Result;
import com.helei.dto.request.AddKLineRequest;
import com.helei.realtimedatacenter.service.impl.market.BinanceMarketRTDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/realtime")
public class RealtimeDataCenterController {


    @Autowired
    private BinanceMarketRTDataService binanceMarketRTDataService;

    @GetMapping("/addKLine")
    public Result addKLine(AddKLineRequest request) {
        CEXType cexType = request.getCexType();

        switch (cexType) {
            case BINANCE -> {
                Set<String> set = binanceMarketRTDataService.startSyncEnvSymbolIntervalsKLine(request.getRunEnv(), request.getTradeType(), request.getKlineList());
                return Result.ok(set);
            }
            default -> {
                return Result.fail("不支持的请求参数");
            }
        }
    }

}
