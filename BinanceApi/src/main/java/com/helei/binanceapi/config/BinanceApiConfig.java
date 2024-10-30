package com.helei.binanceapi.config;


import com.helei.constants.CEXType;
import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;

@Data
public class BinanceApiConfig implements Serializable {
    private static final String CONFIG_FILE = "binance-api-config.yaml";

    public static final CEXType cexType = CEXType.BINANCE;

    public static final BinanceApiConfig INSTANCE;

    private BinanceURL test_net;

    private BinanceURL normal;

    private InetSocketAddress proxy;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = BinanceApiConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found: " + CONFIG_FILE);
            }
            Map<String, Object> yamlData = yaml.load(inputStream);
            Map<String, Object> shinano = (Map<String, Object>) yamlData.get("shinano");
            Map<String, Object> quantity = (Map<String, Object>) shinano.get("quantity");
            Map<String, Object> api = (Map<String, Object>) quantity.get("api");
            Map<String, Object> binance = (Map<String, Object>) api.get("binance");

            INSTANCE = yaml.loadAs(yaml.dump(binance), BinanceApiConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load YAML file: " + CONFIG_FILE, e);
        }
    }

    /**
     * 获取环境url
     * @param runEnv runEnv
     * @param tradeType tradeType
     * @return BinanceTypedUrl
     */
    public BinanceTypedUrl getEnvUrlSet(RunEnv runEnv, TradeType tradeType) {
        return switch (runEnv) {
            case NORMAL -> normal.getMarketUrlByTradeType(tradeType);
            case TEST_NET -> test_net.getMarketUrlByTradeType(tradeType);
        };
    }

    @Data
    public static class BinanceURL implements Serializable  {
        /**
         * 现货相关 api
         */
        private BinanceTypedUrl spot;

        /**
         * 合约相关 api
         */
        private BinanceTypedUrl u_contract;

        /**
         * 根据交易类型获取市场api
         * @param tradeType tradeType
         * @return market url
         */
        public BinanceTypedUrl getMarketUrlByTradeType(TradeType tradeType) {
            return switch (tradeType) {
                case SPOT -> spot;
                case CONTRACT -> u_contract;
            };
        }
    }

    @Data
    public static class BinanceTypedUrl implements Serializable  {

        /**
         * rest api url
         */
        private String rest_api_url;

        /**
         * 市场数据 api
         */
        private String ws_market_url;

        /**
         * 市场数据stream api, 推送使用
         */
        private String ws_market_stream_url;

        /**
         * 账户信息推送api
         */
        private String ws_account_info_stream_url;
    }

    public static void main(String[] args) {
        System.out.println(INSTANCE);
    }
}
