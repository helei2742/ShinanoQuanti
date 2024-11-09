package com.helei.binanceapi.api.ws;

import com.alibaba.fastjson.JSONObject;
import com.helei.binanceapi.BinanceWSReqRespApiClient;
import com.helei.binanceapi.supporter.IpWeightSupporter;
import com.helei.constants.trade.TradeType;
import com.helei.dto.ASKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BinanceWSAccountApiTest {
   static BinanceWSReqRespApiClient client;

   static ASKey asKey_TestNet = new ASKey("b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66", "a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f");

    @BeforeAll
    public static void setup() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        client = new BinanceWSReqRespApiClient("wss://testnet.binancefuture.com/ws-fapi/v1", new IpWeightSupporter(""));
        client.setTradeType(TradeType.SPOT);
        client.connect().get();
    }

    @Test
    void accountBalance() {
    }

    @Test
    void accountStatus() throws ExecutionException, InterruptedException {

        CompletableFuture<JSONObject> accountStatus = client.getAccountApi().accountStatus(asKey_TestNet, false);

        System.out.println(accountStatus.get());

        TimeUnit.MINUTES.sleep(100);
    }

    @Test
    void accountRateLimitsOrders() {
    }

    @Test
    void accountAllOrders() {
    }

    @Test
    void accountTrades() {
    }
}
