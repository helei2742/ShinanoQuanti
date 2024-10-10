package com.helei.tradedatacenter.support;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Properties;

public class KLineTradingDecision {

    public static void main(String[] args) throws Exception {
        // 设置 Flink 流环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Kafka 配置
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("group.id", "kline-consumer");

        // 从 Kafka 中读取 K 线数据
        FlinkKafkaConsumer<String> kafkaSource = new FlinkKafkaConsumer<>(
                "kline_topic",
                new SimpleStringSchema(),
                properties
        );

        kafkaSource.assignTimestampsAndWatermarks(
                WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(5))
        );

        // 解析 K 线数据 (假设 K 线数据是 JSON 格式)
        DataStream<KLine> klineStream = env.addSource(kafkaSource)
                .map(line -> parseKLine(line));  // 解析成 KLine 对象

        // 基于股票代码分区，并计算移动平均线 (SMA) 和 RSI
        klineStream.keyBy(KLine::getSymbol)
                .process(new TradingStrategy())
                .print();

        env.execute("Flink K-Line Trading Decision");
    }

    // 定义 K 线数据类
    public static class KLine {
        public String symbol;  // 股票或交易对
        public long timestamp; // 时间戳
        public double open;    // 开盘价
        public double close;   // 收盘价
        public double high;    // 最高价
        public double low;     // 最低价
        public double volume;  // 成交量

        public String getSymbol() {
            return symbol;
        }
    }

    // 解析 JSON 数据（简化，假设输入是 JSON 格式的 K 线数据）
    public static KLine parseKLine(String line) {
        // 这里可以使用任何 JSON 解析库来解析 K 线数据
        // 假设返回一个 KLine 对象
        return new KLine();  // 替换为真实解析逻辑
    }

    // 交易策略，计算 SMA 和 RSI 并做出决策
    public static class TradingStrategy extends KeyedProcessFunction<String, KLine, String> {

        // 移动平均线状态
        private transient ValueState<Double> smaState;
        private transient ValueState<Double> rsiState;

        @Override
        public void open(org.apache.flink.configuration.Configuration parameters) throws Exception {
            // 初始化状态
            smaState = getRuntimeContext().getState(new ValueStateDescriptor<>("sma", Double.class));
            rsiState = getRuntimeContext().getState(new ValueStateDescriptor<>("rsi", Double.class));
        }

        @Override
        public void processElement(KLine kline, Context ctx, Collector<String> out) throws Exception {
            // 获取当前 K 线的收盘价
            double closePrice = kline.close;

            // 获取历史的 SMA 和 RSI
            Double currentSma = smaState.value();
            Double currentRsi = rsiState.value();

            // 如果是第一次计算，初始化 SMA 和 RSI
            if (currentSma == null) currentSma = closePrice;
            if (currentRsi == null) currentRsi = 50.0;  // 初始 RSI 值

            // 计算新的 SMA (简单移动平均线)
            currentSma = (currentSma * 14 + closePrice) / 15;  // 假设 15 天窗口

            // 计算 RSI (相对强弱指数)
            double gain = Math.max(0, closePrice - kline.open);
            double loss = Math.max(0, kline.open - closePrice);
            double avgGain = (gain + (14 - 1) * currentRsi) / 14;
            double avgLoss = (loss + (14 - 1) * (100 - currentRsi)) / 14;
            currentRsi = 100 - (100 / (1 + avgGain / avgLoss));

            // 保存新的状态
            smaState.update(currentSma);
            rsiState.update(currentRsi);

            // 根据 SMA 和 RSI 做出决策
            if (currentSma > closePrice && currentRsi > 70) {
                out.collect("卖出信号: " + kline.symbol + "，价格：" + closePrice);
            } else if (currentSma < closePrice && currentRsi < 30) {
                out.collect("买入信号: " + kline.symbol + "，价格：" + closePrice);
            }
        }
    }
}
