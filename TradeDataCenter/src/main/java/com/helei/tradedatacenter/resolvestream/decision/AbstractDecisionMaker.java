package com.helei.tradedatacenter.resolvestream.decision;

        import com.helei.cexapi.binanceapi.constants.order.BaseOrder;
        import com.helei.tradedatacenter.dto.OriginOrder;
        import com.helei.tradedatacenter.entity.TradeSignal;
        import lombok.Getter;
        import lombok.Setter;
        import lombok.extern.slf4j.Slf4j;
        import org.apache.flink.configuration.Configuration;
        import org.apache.flink.streaming.api.functions.ProcessFunction;
        import org.apache.flink.util.Collector;

        import java.util.List;


@Slf4j
@Getter
@Setter
public abstract class AbstractDecisionMaker extends ProcessFunction<List<TradeSignal>, OriginOrder> {

    private final String name;

    protected AbstractDecisionMaker(String name) {
        this.name = name;
    }

    @Override
    public void open(Configuration parameters) throws Exception {

    }

    @Override
    public void processElement(List<TradeSignal> windowSignal, ProcessFunction<List<TradeSignal>, OriginOrder>.Context context, Collector<OriginOrder> collector) throws Exception {
        if (windowSignal.isEmpty()) {
            log.debug("[{}] 时间窗口内没有信号", name);
        } else {
            log.info("[{}] 当前时间窗口，产生[{}]个信号", name, windowSignal.size());
            OriginOrder order = decisionAndBuilderOrder(windowSignal);

            if (order != null) {
                log.info("[{}] 窗口内信号满足决策下单条件，下单[{}}", name, order);
                collector.collect(order);
            }
        }
    }

    public abstract OriginOrder decisionAndBuilderOrder(List<TradeSignal> windowSignal);
}
