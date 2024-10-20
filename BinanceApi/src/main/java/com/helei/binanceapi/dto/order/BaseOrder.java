package com.helei.binanceapi.dto.order;

import com.helei.constants.TradeSide;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BaseOrder {

    /**
     * 交易对
     */
    private String symbol;

    /**
     * 交易方向
     */
    private TradeSide side;

    /**
     * apikey
     */
    private String apiKey;

    /**
     * 签名
     */
    private String signature;

    private Long recvWindow;

    private Long timestamp;
}
