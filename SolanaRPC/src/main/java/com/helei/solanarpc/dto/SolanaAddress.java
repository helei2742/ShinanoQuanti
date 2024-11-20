package com.helei.solanarpc.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class SolanaAddress {


    /**
     * 监听地址的名字
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 账户地址
     */
    private String accountAddress;

    /**
     * 开始计算的时间
     */
    private long calStartTime;

    /**
     * 从calStartTIme开始收益的SOL数
     */
    private long profitSol;

}
