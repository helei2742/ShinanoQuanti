package com.helei.config;

import com.helei.constants.KLineInterval;
import com.helei.dto.SymbolKLineInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Component
@ConfigurationProperties(prefix = "shinano.quantity.realtime.contract")
public class RealtimeContractDataConfig {

    private List<String> listenKLine;

    /**
     * 客户端监听k线最大的数量
     */
    private int clientListenKLineMaxCount = 20;

    /**
     * 实时的k线种类
     */
    private List<SymbolKLineInfo> realtimeKLineList;


    public void setListenKLine(List<String> listenKLine) {
        realtimeKLineList = listenKLine.stream().map(s -> {
            String[] split = s.split("@");
            String symbol = split[0];
            Set<KLineInterval> set = Arrays.stream(split[1].split(","))
                    .map(KLineInterval.STATUS_MAP::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return new SymbolKLineInfo(symbol, set);
        }).collect(Collectors.toList());

        this.listenKLine = listenKLine;
    }

}
