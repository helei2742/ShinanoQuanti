package com.helei.dto.request;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson.JSONObject;
import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.trade.KLineInterval;
import com.helei.constants.trade.TradeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddKLineRequest {

    private RunEnv runEnv;

    private TradeType tradeType;

    private CEXType cexType;

    private List<Pair<String, KLineInterval>> klineList;


    public static void main(String[] args) {
        AddKLineRequest addKLineRequest = new AddKLineRequest(RunEnv.TEST_NET, TradeType.CONTRACT, CEXType.BINANCE, new ArrayList<>());
        addKLineRequest.getKlineList().add(new Pair<>("aw1", KLineInterval.M_1));

        String string = JSONObject.toJSONString(addKLineRequest);
        System.out.println(string);
        System.out.println(JSONObject.parseObject(string, AddKLineRequest.class));
    }
}
