package com.helei.cexapi.binanceapi.constants.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class BaseOrder {

    private String symbol;

    private TradeSide side;

    private Long recvWindow;

    private Long timestamp;
}
