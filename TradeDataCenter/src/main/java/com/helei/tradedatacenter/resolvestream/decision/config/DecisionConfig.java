package com.helei.tradedatacenter.resolvestream.decision.config;


import lombok.*;

import java.io.Serializable;

/**
 * 决策条件
 */
@Data
public abstract class DecisionConfig implements Serializable {

    private final String name;

    public DecisionConfig(String name) {
        this.name = name;
    }
}
