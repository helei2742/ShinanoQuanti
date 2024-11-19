package com.helei.solanarpc;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.netty.base.AbstractWebsocketClient;
import com.helei.snowflack.SnowFlakeFactory;
import com.helei.solanarpc.constants.SolanaCommitment;
import com.helei.solanarpc.constants.SolanaWSRequestType;
import com.helei.solanarpc.dto.SolanaWSRequestContext;
import com.helei.solanarpc.support.SolanaEventInvocation;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class SolanaWebsocketClient extends AbstractWebsocketClient<JSONObject, JSONObject> {


    private static final SnowFlakeFactory snoFlakeFactory = new SnowFlakeFactory(2, 2);


    public SolanaWebsocketClient(String url) {
        super(url, new SolanaWebsocketClientHandler());
    }


    /**
     * 订阅指定地址的日志
     *
     * @param address    地址
     * @param invocation 回调
     * @return 订阅id
     *
     * 推送消息：
     *{
     *   "jsonrpc": "2.0",
     *   "method": "logsNotification",
     *   "params": {
     *     "subscription": 1,
     *     "result": {
     *       "context": {
     *         "slot": 12345678
     *       },
     *       "value": {
     *         "signature": "交易签名",
     *         "logMessages": [
     *           "Program xxx invoked by...",
     *           "Transfer 10 SOL from ... to ..."
     *         ]
     *       }
     *     }
     *   }
     * }
     */
    public CompletableFuture<Long> logsSubscribe(String address, SolanaEventInvocation invocation) {
        JSONArray params = new JSONArray();

        JSONObject jb1 = new JSONObject();
        JSONArray ja1 = new JSONArray();
        ja1.add(address);
        jb1.put("mentions", ja1);
        params.add(jb1);

        JSONObject jb2 = new JSONObject();
        jb2.put("encoding", "jsonParsed");
        params.add(jb2);

        return sendSolanaWSRequest(SolanaWSRequestType.logsSubscribe, address, null, params, invocation);
    }

    /**
     * 订阅某个账户的状态变化，包括余额或数据更新。
     *
     * @param address 需要订阅的账户地址。
     * @param commitment 数据确认等级（processed、confirmed、finalized）。
     * @param invocation 收到事件的回调
     * @return
     *推送示例：
     * {
     *   "jsonrpc": "2.0",
     *   "method": "accountNotification",
     *   "params": {
     *     "subscription": 2,
     *     "result": {
     *       "context": {
     *         "slot": 12345678
     *       },
     *       "value": {
     *         "lamports": 1000000000, // 账户余额
     *         "owner": "TokenProgram1111111111111111111111111",
     *         "data": [], // 账户数据
     *         "executable": false,
     *         "rentEpoch": 345
     *       }
     *     }
     *   }
     * }
     */
    public CompletableFuture<Long> accountSubscribe(String address, SolanaCommitment commitment, SolanaEventInvocation invocation) {
        JSONArray params = new JSONArray();

        params.add(address);
        JSONObject jb2 = new JSONObject();
        jb2.put("encoding", "jsonParsed");
        jb2.put("commitment", commitment.name());
        params.add(jb2);

        return sendSolanaWSRequest(SolanaWSRequestType.accountSubscribe, address, null, params, invocation);
    }


    /**
     * 订阅某个程序（智能合约）相关的账户变化。例如，监控某个代币合约的所有账户。
     *
     * @param programId     程序 ID（合约地址）
     * @param commitment    数据确认等级（processed、confirmed、finalized）。
     * @param invocation    回调
     * @return
     *推送示例：
     * {
     *   "jsonrpc": "2.0",
     *   "method": "programNotification",
     *   "params": {
     *     "subscription": 3,
     *     "result": {
     *       "context": {
     *         "slot": 12345678
     *       },
     *       "value": {
     *         "pubkey": "账户地址",
     *         "account": {
     *           "lamports": 2000000,
     *           "owner": "TokenProgram1111111111111111111111111",
     *           "data": [],
     *           "executable": false,
     *           "rentEpoch": 345
     *         }
     *       }
     *     }
     *   }
     * }
     */
    public CompletableFuture<Long> programSubscribe(String programId, SolanaCommitment commitment, SolanaEventInvocation invocation) {
        JSONArray params = new JSONArray();

        params.add(programId);

        JSONObject jb2 = new JSONObject();
        jb2.put("encoding", "base64");
        jb2.put("commitment", commitment.name());
        params.add(jb2);

        return sendSolanaWSRequest(SolanaWSRequestType.programSubscribe, programId, null, params, invocation);
    }


    /**
     *订阅新区块槽位的变化事件。
     * @param invocation 回调
     * @return
     *推送示例：
     * {
     *   "jsonrpc": "2.0",
     *   "method": "slotNotification",
     *   "params": {
     *     "subscription": 4,
     *     "result": {
     *       "parent": 12345677,
     *       "slot": 12345678,
     *       "root": 12345676
     *     }
     *   }
     * }
     */
    public CompletableFuture<Long> slotSubscribe(SolanaEventInvocation invocation) {
        JSONArray params = new JSONArray();

        return sendSolanaWSRequest(SolanaWSRequestType.slotSubscribe, null, null, params, invocation);
    }


    /**
     * 发送订阅请求
     *
     * @param request    请求类型
     * @param address    地址
     * @param params     参数
     * @param invocation 时间的回调
     * @return 订阅id future
     */
    public CompletableFuture<Long> sendSolanaWSRequest(SolanaWSRequestType request, String address, Long unsubId,JSONArray params, SolanaEventInvocation invocation) {
        long requestId = snoFlakeFactory.nextId();

        JSONObject rb = new JSONObject();
        rb.put("jsonrpc", "2.0");
        rb.put("method", request.name());
        rb.put("id", requestId);
        rb.put("params", params);

        return super.sendRequest(rb).thenApplyAsync(response -> {
            log.info("收到消息[{}]", response);
            Long subscription = response.getLong("result");

            if (subscription != null) {
                // 订阅
                log.info("账户/合约地址[{}]订阅[{}], 订阅id为[{}]", address, request, subscription);

                // 更新订阅id map content
                getClientHandler().getSubscribeIdMapContext().put(
                        subscription,
                        SolanaWSRequestContext
                                .builder()
                                .requestId(requestId)
                                .requestType(request)
                                .address(address)
                                .invocation(invocation)
                                .build()
                );

            } else {
                // 取消订阅
                Boolean unsubResult = response.getBoolean("result");
                log.info("账户/合约地址[{}]取消订阅[{}], status[{}]", address, request, unsubResult);

                if (BooleanUtil.isTrue(unsubResult)) {
                    SolanaWSRequestContext solanaWSRequestContext = getClientHandler().getSubscribeIdMapContext().remove(unsubId);

                    if (solanaWSRequestContext == null) {
                        log.warn("没有[{}]的订阅信息", unsubId);
                        return null;
                    }
                }
            }

            return subscription;
        }, callbackInvoker);
    }


    /**
     * 获取clientHandler
     *
     * @return SolanaWebsocketClientHandler
     */
    private SolanaWebsocketClientHandler getClientHandler() {
        return (SolanaWebsocketClientHandler) handler;
    }
}
