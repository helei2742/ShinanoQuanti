package com.helei.solanarpc;

import cn.hutool.http.HttpException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.snowflack.SnowFlakeFactory;
import com.helei.solanarpc.constants.SolanaHttpRequestType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class SolanaRestHttpClient {

    private final String url;

    private final OkHttpClient okHttpClient;

    private final ExecutorService executor;

    private final SnowFlakeFactory snowFlakeFactory = new SnowFlakeFactory(3, 23);

    public SolanaRestHttpClient(InetSocketAddress proxy, String url, ExecutorService executor) {
        this.url = url;
        this.executor = executor;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (proxy != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, proxy));
        }
        this.okHttpClient = builder.build();
    }


    /**
     * 模拟交易
     *
     * @param transactionSignature "签名后的交易数据(Base64编码)
     * @return
     *{
     *   "jsonrpc": "2.0",
     *   "id": 8,
     *   "result": {
     *     "context": {
     *       "slot": 12345678
     *     },
     *     "value": {
     *       "err": null,
     *       "logs": [
     *         "Program xxx invoked by...",
     *         "Transfer 10 SOL from ... to ..."
     *       ]
     *     }
     *   }
     * }
     */
    public CompletableFuture<JSONObject> simulateTransaction(String transactionSignature) {
        JSONArray params= new JSONArray();
        params.add(transactionSignature);

        JSONObject jb1 = new JSONObject();
        jb1.put("encoding", "base64");
        jb1.put("commitment", "processed");
        params.add(jb1);

        return solanaRestRequest(SolanaHttpRequestType.simulateTransaction, params);
    }


    /**
     *  发送交易
     *
     * @param transactionSignature "签名后的交易数据(Base64编码)
     * @return
     *{
     *   "jsonrpc": "2.0",
     *   "id": 7,
     *   "result": "交易签名"
     * }
     */
    public CompletableFuture<JSONObject> sendTransaction(String transactionSignature) {
        JSONArray params= new JSONArray();
        params.add(transactionSignature);

        JSONObject jb1 = new JSONObject();
        jb1.put("encoding", "base64");
        jb1.put("commitment", "confirmed");
        params.add(jb1);

        return solanaRestRequest(SolanaHttpRequestType.sendTransaction, params);
    }

    /**
     * 获取区块健康状态
     *
     * @return
     *{
     *   "jsonrpc": "2.0",
     *   "id": 6,
     *   "result": "ok" // 表示网络健康
     * }
     */
    public CompletableFuture<JSONObject> getHealth() {
        JSONArray params= new JSONArray();
        return solanaRestRequest(SolanaHttpRequestType.getHealth, params);
    }

    /**
     * 获取多个账户的信息
     *
     * @param accountAddressArray 账户地址数组
     * @return
     * {
     * "jsonrpc": "2.0",
     *   "id": 5,
     *   "result": {
     *     "context": {
     *       "slot": 12345678
     *     },
     *     "value": [
     *       {
     *         "data": [],
     *         "lamports": 1000000000,
     *         "owner": "11111111111111111111111111111111",
     *         "executable": false,
     *         "rentEpoch": 345
     *       },
     *       {
     *         "data": [],
     *         "lamports": 2000000000,
     *         "owner": "11111111111111111111111111111111",
     *         "executable": false,
     *         "rentEpoch": 345
     *       }
     *     ]
     *   }
     * }
     */
    public CompletableFuture<JSONObject> getMultipleAccounts(JSONArray accountAddressArray) {
        JSONArray params= new JSONArray();
        params.add(accountAddressArray);

        JSONObject jb1 = new JSONObject();
        jb1.put("encoding", "jsonParsed");
        jb1.put("commitment", "finalized");
        params.add(jb1);

        return solanaRestRequest(SolanaHttpRequestType.getMultipleAccounts, params);
    }

    /**
     * 获取区块信息
     *
     * @param slotId slot id
     * @return
     * {
     *   "jsonrpc": "2.0",
     *   "id": 4,
     *   "result": {
     *     "blockhash": "区块哈希",
     *     "parentSlot": 12345677,
     *     "transactions": [
     *       {
     *         "transaction": {
     *           "signatures": ["签名"],
     *           "message": {
     *             "accountKeys": ["账户地址1", "账户地址2"],
     *             "instructions": [
     *               {
     *                 "programId": "TokenProgram1111111111111111111111111",
     *                 "data": "指令数据",
     *                 "accounts": ["账户地址1", "账户地址2"]
     *               }
     *             ],
     *             "recentBlockhash": "区块哈希"
     *           }
     *         },
     *         "meta": {
     *           "err": null,
     *           "fee": 5000,
     *           "preBalances": [1000000000, 2000000000],
     *           "postBalances": [999995000, 2000000000]
     *         }
     *       }
     *     ],
     *     "rewards": []
     *   }
     * }
     */
    public CompletableFuture<JSONObject> getBlock(String slotId) {
        JSONArray params= new JSONArray();
        params.add(slotId);

        JSONObject jb1 = new JSONObject();
        jb1.put("encoding", "jsonParsed");
        jb1.put("transactionDetails", "jsonParsed");
        jb1.put("rewards", true);
        params.add(jb1);

        return solanaRestRequest(SolanaHttpRequestType.getBlock, params);
    }

    /**
     * 获取最新区块
     *
     * @return
     *{
     *   "jsonrpc": "2.0",
     *   "id": 3,
     *   "result": {
     *     "context": {
     *       "slot": 12345678
     *     },
     *     "value": {
     *       "blockhash": "区块哈希",
     *       "lastValidBlockHeight": 12346000
     *     }
     *   }
     * }
     */
    public CompletableFuture<JSONObject> getLatestBlockhash() {
        JSONArray params= new JSONArray();

        JSONObject jb1 = new JSONObject();
        jb1.put("commitment", "finalized");
        params.add(jb1);

        return solanaRestRequest(SolanaHttpRequestType.getLatestBlockhash, params);
    }


    /**
     * 获取交易详情
     *
     * @param signature 交易签名
     * @return
     * {
     *   "jsonrpc": "2.0",
     *   "id": 2,
     *   "result": {
     *     "slot": 12345678,
     *     "transaction": {
     *       "signatures": ["签名"],
     *       "message": {
     *         "accountKeys": [
     *           "账户1地址",
     *           "账户2地址"
     *         ],
     *         "instructions": [
     *           {
     *             "programId": "TokenProgram1111111111111111111111111",
     *             "data": "指令数据",
     *             "accounts": ["账户1地址", "账户2地址"]
     *           }
     *         ],
     *         "recentBlockhash": "区块哈希"
     *       }
     *     },
     *     "meta": {
     *       "err": null,
     *       "fee": 5000,
     *       "preBalances": [1000000000, 2000000000],
     *       "postBalances": [999995000, 2000000000],
     *       "preTokenBalances": [],
     *       "postTokenBalances": []
     *     }
     *   }
     * }
     */
    public CompletableFuture<JSONObject> getTransaction(String signature) {
        JSONArray params= new JSONArray();
        params.add(signature);

        JSONObject jb1 = new JSONObject();
        jb1.put("encoding", "jsonParsed");
        params.add(jb1);

        return solanaRestRequest(SolanaHttpRequestType.getTransaction, params);
    }

    /**
     * 获取账户信息
     *
     * @param address address
     * @return
     * {
     *   "jsonrpc": "2.0",
     *   "id": 1,
     *   "result": {
     *     "context": {
     *       "slot": 12345678
     *     },
     *     "value": {
     *       "data": [],  // 账户数据
     *       "executable": false,
     *       "lamports": 1000000000, // 余额 (单位: lamports)
     *       "owner": "11111111111111111111111111111111",
     *       "rentEpoch": 123
     *     }
     *   }
     * }
     */
    public CompletableFuture<JSONObject> getAccountInfo(String address) {
        JSONArray params= new JSONArray();
        params.add(address);

        JSONObject jb1 = new JSONObject();
        jb1.put("encoding", "jsonParsed");
        jb1.put("commitment", "finalized");
        params.add(jb1);

        return solanaRestRequest(SolanaHttpRequestType.getAccountInfo, params);
    }


    /**
     * solana http 请求
     *
     * @param requestType 请求类型
     * @param params      参数
     * @return 结果
     */
    public CompletableFuture<JSONObject> solanaRestRequest(SolanaHttpRequestType requestType, JSONArray params) {

        return CompletableFuture.supplyAsync(() -> {
            Request.Builder builder = new Request.Builder();
            builder.addHeader("Content-Type", "application/json");

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("jsonrpc", "2.0");
            bodyJson.put("id", snowFlakeFactory.nextId());
            bodyJson.put("method", requestType.name());
            bodyJson.put("params", params);

            RequestBody requestBody = RequestBody.create(bodyJson.toJSONString().getBytes(StandardCharsets.UTF_8));

            builder.url(url).post(requestBody);

            try (Response response = okHttpClient.newCall(builder.build()).execute()) {
                if (response.isSuccessful()) {
                    return response.body() == null ? new JSONObject() : JSONObject.parseObject(response.body().string());
                } else {
                    throw new HttpException(String.format("请求url [%s] 失败,code [%s], %s", url, response.code(), response.body() == null ? "" : response.body().string()));
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("请求solana rest api [%s],method[%s]发生错误", url, requestType), e);
            }
        }, executor);
    }
}

