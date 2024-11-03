package com.helei.dto.trade;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceInfo {

    private String asset;

    private double free;

    private double locked;
}
