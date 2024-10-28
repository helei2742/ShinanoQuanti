






package com.helei.tradesignalcenter.stream;

import com.helei.tradesignalcenter.dto.OriginOrder;
import com.helei.dto.KLine;
import com.helei.dto.TradeSignal;
import com.helei.tradesignalcenter.stream.c_signal.TradeSignalService;
import com.helei.tradesignalcenter.stream.d_decision.DecisionMakerService;
import com.helei.tradesignalcenter.stream.e_order.AbstractOrderCommitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;

import java.util.List;

@Slf4j
public class AutoTradeTask {


    /**
     * 信号流服务
     */
    private final TradeSignalService tradeSignalService;

    /**
     * 决策服务
     */
    private final DecisionMakerService decisionMakerService;

    /**
     * 订单提交器
     */
    private final AbstractOrderCommitter<OriginOrder> abstractOrderCommitter;


    public AutoTradeTask(
            TradeSignalService tradeSignalService,
            DecisionMakerService decisionMakerService,
            AbstractOrderCommitter<OriginOrder> abstractOrderCommitter
    ) {
        this.tradeSignalService = tradeSignalService;

        this.decisionMakerService = decisionMakerService;

        this.abstractOrderCommitter = abstractOrderCommitter;
    }


    public void execute(String name) throws Exception {

        //1.信号服务
        KeyedStream<Tuple2<KLine, List<TradeSignal>>, String> symbolGroupSignalStream = tradeSignalService.getSymbolGroupSignalStream();

        symbolGroupSignalStream.print();
        //2.决策服务
        DataStream<OriginOrder> originOrderStream = decisionMakerService.decision(symbolGroupSignalStream);


        originOrderStream.sinkTo(abstractOrderCommitter.getCommitSink());
        tradeSignalService.getEnv().execute(name);
    }

}





shinano:
quantity:
        # 信号生成服务配置
trade_signal_maker:
        # name
name: test
      # symbol
symbol: btcusdt
      # 运行环境
run_env: NORMAL
      # 交易类型
trade_type: CONTRACT
      # 历史k线加载批大小
historyKLineBatchSize: 200
        # 批加载的网络并发度
batchLoadConcurrent: 10
        # 实时数据配置
realtime:
        # kafka配置
kafka:
bootstrapServer: 192.168.1.2:9092
groupId: trade_signal_app_test
        # flink配置
flink:
jobManagerHost: 192.168.1.2
jobManagerPort: 8081









asKey:
test:
spot:
apiKey: 1JIhkPyK07xadG9x8hIwqitN95MgpypPzA4b6TLraTonRnJ8BBJQlaO2iL9tPH0Y
secretKey: t84TYFR1zieMGncbw3kYq4zAPLxIJHJeMdD8V0FMKxij9fApojV6bhbDpyyjNDWt
contract:
apiKey: b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66
secretKey: a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f















