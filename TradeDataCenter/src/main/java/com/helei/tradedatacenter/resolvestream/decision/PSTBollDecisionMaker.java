package com.helei.tradedatacenter.resolvestream.decision;


import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
        import com.helei.tradedatacenter.entity.TradeSignal;

/**
 * 根据PST和Boll指标决策下单
 */
public class PSTBollDecisionMaker extends AbstractDecisionMaker {

    private final String bollKey;

    private final String pstKey;


    public PSTBollDecisionMaker(String bollKey, String pstKey) {
        this.bollKey = bollKey;
        this.pstKey = pstKey;
    }


    @Override
    public BaseOrder decisionAndBuilderOrder(TradeSignal signal) {


        return null;
    }
}