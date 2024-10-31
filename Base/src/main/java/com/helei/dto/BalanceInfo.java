package com.helei.dto;

import lombok.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BalanceInfo {

    private String asset;

    private double free;

    private double locked;
}
