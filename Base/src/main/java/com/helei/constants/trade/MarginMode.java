package com.helei.constants.trade;


import java.util.Arrays;

/**
 * 保证金模式
 */
public enum MarginMode {
    /**
     * 全仓模式
     */
    CROSSED,

    CROSS,
    /**
     * 逐仓模式
     */
    ISOLATED
    ;

    public static void main(String[] args) {

        System.out.println(Arrays.toString(MarginMode.values()));
    }
}
