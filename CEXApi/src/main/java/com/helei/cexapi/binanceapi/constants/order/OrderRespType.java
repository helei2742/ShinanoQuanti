

package com.helei.cexapi.binanceapi.constants.order;

        import lombok.Getter;

/**
 * 可选的响应格式: ACK，RESULT，FULL.
 * MARKET和LIMIT订单默认使用FULL，其他订单类型默认使用ACK。
 */
@Getter
public enum OrderRespType {

    ACK("ACK"),
    RESULT("RESULT"),
    FULL("FULL")
    ;

    private final String describe;

    OrderRespType(String describe) {
        this.describe = describe;
    }


    @Override
    public String toString() {
        return describe;
    }
}