package com.helei.solanarpc;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.solanarpc.constants.SolanaTransactionType;
import com.helei.solanarpc.dto.SolanaAddress;
import com.helei.solanarpc.dto.SolanaTransactionDetail;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

@Slf4j
public class SolanaApiService {

    private final SolanaWebsocketClient solanaWebsocketClient;

    private final SolanaRestHttpClient solanaRestHttpClient;

    private final ExecutorService executor;

    public SolanaApiService(String wsUrl, String httpUrl, InetSocketAddress proxy, ExecutorService executorService) {
        this.solanaRestHttpClient = new SolanaRestHttpClient(proxy, httpUrl, executorService);
        this.solanaWebsocketClient = new SolanaWebsocketClient(wsUrl);
        this.solanaWebsocketClient.setProxy(proxy);
        this.executor = executorService;
    }


    public void init() throws URISyntaxException, SSLException, ExecutionException, InterruptedException {
        this.solanaWebsocketClient.connect().get();
    }


    /**
     * 监听地址的交易
     *
     * @param address        address
     * @param detailConsumer detailConsumer
     * @return CompletableFuture<Long>
     */
    public CompletableFuture<Long> listenAddressTransition(SolanaAddress address, BiConsumer<SolanaAddress, SolanaTransactionDetail> detailConsumer) {
        //Step 1 监听 地址的log
        return solanaWebsocketClient.logsSubscribe(address.getAccountAddress(), (eventType, context, event) -> {
            JSONObject value = event.getJSONObject("params").getJSONObject("value");

            //Step 2.从监听事件中获取签名
            String signature = value.getString("signature");

            log.info("监听到账户名[{}]-地址[{}] - 产生交易 signature[{}], 开始获取详细信息",
                    address.getName(), address.getAccountAddress(), signature);


            //Step 3 根据签名获取交易信息
            solanaRestHttpClient.getTransaction(signature).thenAcceptAsync(response -> {
                //Step 4 解析交易消息
                SolanaTransactionDetail detail = getSolanaTransactionDetail(address.getAccountAddress(), response);
                detail.setTransactionSignature(signature);

                log.info("账户名[{}]-地址[{}] - 交易信息[{}]", address.getName(), address.getAccountAddress(), detail);

                // Step 5 调用回调方法
                detailConsumer.accept(address, detail);
            }, executor).exceptionally(throwable -> {
                log.error("处理地址[{}]交易信息发生错误", address, throwable);
                return null;
            });
        }).exceptionally(throwable -> {
            log.error("监听地址[{}]的log信息发生错误", address, throwable);
            return null;
        });
    }


    /**
     * 解析交易信息
     *
     * @param response response
     * @return SolanaTransactionDetail
     */
    private static SolanaTransactionDetail getSolanaTransactionDetail(String sourceAddress, JSONObject response) {
        JSONObject result = response.getJSONObject("result");

        JSONObject meta = result.getJSONObject("meta");

        JSONObject transaction = result.getJSONObject("transaction");


        // 交易源
        String targetAddress = "";

        SolanaTransactionType transactionType = null;

        for (int i = 0; i < meta.getJSONArray("innerInstructions").size(); i++) {
            if (transactionType != null) break;

            JSONArray instructions = meta.getJSONArray("innerInstructions").getJSONObject(i).getJSONArray("instructions");

            for (int i1 = 0; i1 < instructions.size(); i1++) {
                JSONObject instruction = instructions.getJSONObject(i1);

                // 有spl-token，是swap
                if (instruction.containsKey("parsed") && "spl-token".equals(instruction.getString("program"))) {
                    JSONObject info = instruction.getJSONObject("parsed").getJSONObject("info");

                    targetAddress = StrUtil.isBlank(info.getString("authority"))
                            ? info.getString("destination") : info.getString("authority");

                    transactionType = SolanaTransactionType.SEND_SPL_TOKEN;
                    break;
                }
            }
        }

        // token 交易信息
        JSONArray preTokenBalances = meta.getJSONArray("preTokenBalances");
        JSONArray postTokenBalances = meta.getJSONArray("postTokenBalances");


        Long fee = meta.getLong("fee");

        SolanaTransactionDetail.SolanaTransactionDetailBuilder builder = SolanaTransactionDetail
                .builder()
                .sourceAccountAddress(sourceAddress)
                .targetAccountAddress(targetAddress)
                .fee(fee);

        if (preTokenBalances != null && !preTokenBalances.isEmpty()) {
            // 代币swap，sol换其它币，或其它币换sol， 或者其它代币换其它代币

            // token 变化信息
            for (int i = 0; i < preTokenBalances.size(); i++) {
                JSONObject preBalance = preTokenBalances.getJSONObject(i);
                JSONObject postBalance = postTokenBalances.getJSONObject(i);

                // 获取token数量变化
                double preAmount = preBalance.getJSONObject("uiTokenAmount").getDoubleValue("uiAmount");
                double postAmount = postBalance.getJSONObject("uiTokenAmount").getDoubleValue("uiAmount");
                double amountTransferred = postAmount - preAmount;

                builder.tokenMint(preBalance.getString("mint"));

                if (targetAddress.equals(preBalance.getString("owner"))) {

                    builder.targetTokenAmount(postAmount);
                    builder.targetTokenTransfer(amountTransferred);
                } else if (sourceAddress.equals(preBalance.getString("owner"))) {

                    builder.sourceTokenAmount(postAmount);
                    builder.sourceTokenTransfer(amountTransferred);
                }
            }
        } else {
            // sol转账
            transactionType = SolanaTransactionType.SEND_SOL;
        }

        JSONArray addressArray = transaction.getJSONObject("message").getJSONArray("accountKeys");

        Integer sourceIdx = null;
        Integer targetIdx = null;
        if (transactionType.equals(SolanaTransactionType.SEND_SOL)) {
            sourceIdx = 0;
            targetIdx = 1;
        } else {
            for (int i = 0; i < addressArray.size(); i++) {
                String pubkey = addressArray.getJSONObject(i).getString("pubkey");
                if (pubkey.equals(sourceAddress)) {
                    sourceIdx = i;
                } else if (pubkey.equals(targetAddress)) {
                    targetIdx = i;
                }

                if (sourceIdx != null && targetIdx != null) break;
            }
        }


        // 账户sol信息
        JSONArray preBalancesLamports = meta.getJSONArray("preBalances");
        JSONArray postBalancesLamports = meta.getJSONArray("postBalances");

        builder.sourceSolAmount(postBalancesLamports.getLong(sourceIdx))
                .sourceSolTransfer(postBalancesLamports.getLong(sourceIdx) - preBalancesLamports.getLong(sourceIdx))
                .targetSolAmount(postBalancesLamports.getLong(targetIdx))
                .targetSolTransfer(postBalancesLamports.getLong(targetIdx) - preBalancesLamports.getLong(targetIdx))
                .error(meta.getBoolean("error"));


        return builder.solanaTransactionType(transactionType).build();
    }


    public static void main(String[] args) throws IOException {
        Path path = Paths.get("D:\\workspace\\ideaworkspace\\ShinanoQuanti-main\\test.json");

        byte[] bytes = Files.readAllBytes(path);

        String text = new String(bytes);
        JSONObject jsonObject = JSONObject.parseObject(text);

        SolanaTransactionDetail detail = getSolanaTransactionDetail("EBnKTDJxUCPzLQDbFE3gsvpiBwq9UiR5NWja6EZtSw3z", jsonObject);
        System.out.println(JSONObject.toJSONString(detail));
    }
}

