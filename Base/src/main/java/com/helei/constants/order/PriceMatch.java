package com.helei.constants.order;

/**
 * 盘口价下单模式:
 */
public enum PriceMatch {
    NONE,
    /**
     *  (盘口对手价)
     */
    OPPONENT,
    /**
     * (盘口对手5档价)
     */
    OPPONENT_5,
    /**
     * (盘口对手10档价)
     */
    OPPONENT_10,
    /**
     * (盘口对手20档价)
     */
    OPPONENT_20,
    /**
     * (盘口同向价)
     */
    QUEUE,
    /**
     * (盘口同向排队5档价)
     */
    QUEUE_5,
    /**
     *  (盘口同向排队10档价)
     */
    QUEUE_10,
    /**
     * (盘口同向排队20档价)
     */
    QUEUE_20,
    ;
}

