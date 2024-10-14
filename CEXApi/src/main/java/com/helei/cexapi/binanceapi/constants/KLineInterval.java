
package com.helei.cexapi.binanceapi.constants;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

public enum KLineInterval implements Serializable {


    m_1("1m", 60),
    m_3("3m", 3 * 60),
    m_5("5m", 5 * 60),
    m_15("15m", 15 * 60),
    m_30("30m", 30 * 60),
    h_1("1h", 60 * 60),
    h_2("2h", 2 * 60 * 60),
    h_4("4h", 4 * 60 * 60),
    h_6("6h", 6 * 60 * 60),
    h_8("8h", 8 * 60 * 60),
    h_12("12h", 12 * 60 * 60),
    d_1("1d", 24 * 60 * 60),
    d_3("3d", 3 * 24 *60 *60),
    w_1("1w", 7 * 24 * 60 * 60),
    M_1("1M", 30 * 24 * 60 * 60),
    ;

    @Serial
    private static final long serialVersionUID = 8882388L; // 显式声明 serialVersionUID

    @Getter
    private final String describe;
    @Getter
    private final long second;


    KLineInterval(String describe, long second) {
        this.describe = describe;
        this.second = second;
    }

    @Override
    public String toString() {
        return describe;
    }
}