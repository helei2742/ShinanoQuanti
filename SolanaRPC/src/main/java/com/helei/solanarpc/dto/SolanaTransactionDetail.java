package com.helei.solanarpc.dto;


import com.helei.solanarpc.constants.SolanaTransactionType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SolanaTransactionDetail {


    /**
     * 交易类型
     */
    private SolanaTransactionType solanaTransactionType;


    /**
     *  Token Mint 地址 (币种)
     */
    private String tokenMint;

    /**
     * 源账户地址, 发起交易的账户地址
     */
    private String sourceAccountAddress;

    /**
     * 源sol数量
     */
    private long sourceSolAmount;

    /**
     * 源token 币的数量
     */
    private Double sourceTokenAmount;

    /**
     * 源sol的变化量
     */
    private long sourceSolTransfer;

    /**
     * 源token币的变化量
     */
    private Double sourceTokenTransfer;



    /**
     * 目标账户地址
     */
    private String targetAccountAddress;
    /**
     * 目标sol数量
     */
    private long targetSolAmount;

    /**
     * 目标token 币的数量
     */
    private Double targetTokenAmount;

    /**
     * 目标sol的变化量
     */
    private long targetSolTransfer;

    /**
     * 目标token币的变化量
     */
    private Double targetTokenTransfer;


    /**
     * 费用
     */
    private long fee;


    /**
     * 交易是否发生错误
     */
    private Boolean error;


    /**
     * 这笔交易的签名
     */
    private String transactionSignature;
}
