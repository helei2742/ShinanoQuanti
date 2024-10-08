package com.helei.tradedatacenter.subscribe.binanceapi;

import com.helei.tradedatacenter.netty.base.AbstractNettyClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 币安ws接口客户端
 */
@Slf4j
public class BinanceWSApiClient extends AbstractNettyClient {

    public BinanceWSApiClient(String baseUrl, List<String> subscribeList) {
        super(null, 3 * 60, nettyProcessorAdaptor);
        super.uri = generalUri(baseUrl, subscribeList);

    }

    private String generalUri(String baseUrl, List<String> subscribeList) {
        StringBuilder uri = new StringBuilder(baseUrl);

        if (subscribeList.size() == 1) {
            uri.append("/ws").append(subscribeList.get(0));
        } else {
            uri.append("/stream?streams=");
            for (String name : subscribeList) {
                uri.append(name);
            }
        }

        return uri.toString();
    }
}
