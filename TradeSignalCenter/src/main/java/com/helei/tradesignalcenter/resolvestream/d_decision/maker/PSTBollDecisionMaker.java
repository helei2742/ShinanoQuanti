package com.helei.tradesignalcenter.resolvestream.d_decision.maker;


        import com.helei.tradesignalcenter.dto.OriginOrder;
        import com.helei.dto.TradeSignal;
        import com.helei.tradesignalcenter.resolvestream.d_decision.config.PSTBollDecisionConfig_v1;
        import com.helei.dto.indicator.Indicator;
        import com.helei.dto.indicator.config.IndicatorConfig;
        import lombok.extern.slf4j.Slf4j;

        import java.math.BigDecimal;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.stream.Collectors;

/**
 * 根据PST和Boll指标决策下单
 */
@Deprecated
@Slf4j
public class PSTBollDecisionMaker extends AbstractDecisionMaker {

    private final PSTBollDecisionConfig_v1 config;

    public PSTBollDecisionMaker(PSTBollDecisionConfig_v1 config) {
        super(config.getName());
        this.config = config;
    }

    @Override
    protected OriginOrder decisionAndBuilderOrder(String symbol, List<TradeSignal> windowSignal, HashMap<IndicatorConfig<? extends Indicator>, Indicator> indicatorMap) {
        String pstKey = config.getPstConfig().getIndicatorName();
        String bollKey = config.getBollConfig().getIndicatorName();

        Map<String, List<TradeSignal>> signalMap = windowSignal.stream().collect(Collectors.groupingBy(TradeSignal::getName));

        List<TradeSignal> pstSignals = signalMap.get(pstKey);
        List<TradeSignal> bollSignals = signalMap.get(bollKey);

        if (pstSignals == null || bollSignals == null || pstSignals.isEmpty() || bollSignals.isEmpty()) {
            log.warn("pst和boll信号不满足共振， 不生成订单");
            return null;
        }

        TradeSignal newPstSignal = pstSignals.getLast();
        TradeSignal newBollSignal = bollSignals.getLast();

        //TODO 仅仅测试用
        return buildMarketOrder(newBollSignal);
    }

    private static OriginOrder buildMarketOrder(TradeSignal newBollSignal) {
        return OriginOrder
                .builder()
                .symbol(newBollSignal.getKLine().getSymbol())
                .tradeSide(newBollSignal.getTradeSide())
                .targetPrice(BigDecimal.valueOf(newBollSignal.getTargetPrice()))
                .stopPrice(BigDecimal.valueOf(newBollSignal.getStopPrice()))
                .build();
    }
}
