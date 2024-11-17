package com.helei.twiterstream;

import com.alibaba.fastjson.JSONObject;
import com.helei.twiterstream.dto.TwitterFilterRule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.*;


@Slf4j
public class TwitterStreamRuleClient {

    private static final int CONNECT_RETRY_LIMIT = 3;

    private final URL ruleUrl;

    private final URL streamUrl;

    private final String bearerToken;

    private final ExecutorService executor;

    private final TwitterFilterRule filterRule;

    private final BlockingQueue<JSONObject> buffer;

    private volatile boolean isRunning = false;

    @Getter
    private HttpURLConnection streamConnection;

    public TwitterStreamRuleClient(String ruleUrl, String streamUrl, String bearerToken, ExecutorService executor) throws MalformedURLException {
        this.ruleUrl = new URL(ruleUrl);
        this.streamUrl = new URL(streamUrl);
        this.bearerToken = bearerToken;
        this.executor = executor;
        this.filterRule = new TwitterFilterRule();
        this.buffer = new LinkedBlockingQueue<>();
    }

    /**
     * 添加过滤规则
     *
     * @param rule 规则字符串
     * @return this
     */
    public TwitterStreamRuleClient addFilterRule(String rule, String value) {
        filterRule.addRule(rule + ":" + value);
        return this;
    }

    /**
     * 开始监听流
     *
     * @return buffer
     */
    public CompletableFuture<BlockingQueue<JSONObject>> listenToStream() {
        if (isRunning) return CompletableFuture.supplyAsync(()->buffer, executor);

        return requestAddFilterRule()
                .thenApplyAsync(isSuccess -> {
                    if (isSuccess) {
                        BlockingQueue<JSONObject> jsonObjects = null;
                        try {
                            jsonObjects = requestListenStream().get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        return jsonObjects;
                    } else {
                        throw new RuntimeException("请求添加过滤规则失败");
                    }
                }, executor);
    }


    /**
     * 关闭连接
     */
    public void close() {
        isRunning = false;

        if (this.streamConnection != null) {
            log.warn("关闭连接[{}]", streamConnection);
            streamConnection.disconnect();
        }
    }


    /**
     * 请求添加过滤规则
     *
     * @return 是否添加成功
     */
    private CompletableFuture<Boolean> requestAddFilterRule() {
        return CompletableFuture.supplyAsync(() -> {
            RetryConnector retryConnector = new RetryConnector(CONNECT_RETRY_LIMIT, 10, executor);

            return retryConnector.retryConnect(
                    (i, limit) -> {
                        try {
                            HttpURLConnection connection = (HttpURLConnection) ruleUrl.openConnection();
                            connection.setRequestMethod("POST");
                            initConnection(connection);
                            connection.setDoOutput(true);
                            connection.getOutputStream().write(JSONObject.toJSONBytes(filterRule));

                            int responseCode = connection.getResponseCode();
                            if (responseCode == 201) {
                                log.info("[{}/{}] - 规则[{}]添加成功", i, limit, filterRule);
                                return true;
                            } else {
                                log.warn("[{}/{}] - 规则[{}]添加失败code[{}], message[{}], 准备重试",
                                        i, limit, filterRule, responseCode, connection.getResponseMessage());
                                return false;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    String.format("开始向bearerToken[%s]添加规则[%S]", bearerToken, filterRule),
                    String.format("尝试向bearerToken[%s]添加规则[%S]发生异常, url[%s]", bearerToken, filterRule, ruleUrl),
                    String.format("向bearerToken[%s]添加规则[%S]失败, url[%s]", bearerToken, filterRule, ruleUrl)
            );

        }, executor);
    }

    /**
     * 请求监听流
     *
     * @return future<buffer>
     */
    private CompletableFuture<BlockingQueue<JSONObject>> requestListenStream() {
        return CompletableFuture.supplyAsync(() -> {
            isRunning = true;

            RetryConnector retryConnector = new RetryConnector(CONNECT_RETRY_LIMIT, 60, executor);
            try {
                boolean result = retryConnector.retryConnect(
                        (i, limit) -> {
                            try {
                                streamConnection = (HttpURLConnection) streamUrl.openConnection();
                                streamConnection.setRequestMethod("GET");
                                initConnection(streamConnection);

                                streamConnection.connect();

                                //开始从http长连接中取推送数据
                                startLoadTwitterMessage(streamConnection)
                                        //错误异步给他重试
                                        .exceptionallyAsync(throwable -> {
                                            log.error("从Http长连接中获取流数据发生错误, 尝试重新连接. url[{}], bearerToken[{}]", streamUrl, bearerToken);
                                            streamConnection.disconnect();

                                            // client 还在运行， 继续运行调用此方法尝试重启
                                            if (isRunning) {
                                                requestListenStream();
                                            }

                                            return null;
                                        }, executor);

                                return true;
                            } catch (Exception e) {
                                throw new RuntimeException("获取流数据时发生错误", e);
                            }
                        },
                        String.format("开始获取bearerToken[%s]-rule[%s]数据流", bearerToken, filterRule),
                        String.format("获取bearerToken[%s]-rule[%s]数据流发生异常, url[%s]", bearerToken, filterRule, streamUrl),
                        String.format("获取bearerToken[%s]-rule[%S]数据流获取失败, url[%s]", bearerToken, filterRule, streamUrl)
                );

                if (result) return buffer;
                return null;
            } catch (Exception e) {
                close();
                throw new RuntimeException("请求监听流发生错误", e);
            }
        }, executor);
    }


    /**
     * 开始加载k线信息
     *
     * @param connection connection
     * @return void
     */
    private CompletableFuture<Void> startLoadTwitterMessage(HttpURLConnection connection) {
        return CompletableFuture.runAsync(() -> {
            //一直写数据
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.put(JSONObject.parseObject(line));
                }
            } catch (Exception e) {
                throw new RuntimeException("获取流数据时发生错误", e);
            }
        }, executor);
    }


    /**
     * 初始化连接
     *
     * @param connection 连接
     */
    private void initConnection(HttpURLConnection connection) {
        connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
        connection.setRequestProperty("Content-Type", "application/json");
        String s = "rICj2HZrjIcTv63tGiB71qGd1-dZMb0H_7qJT89jhyeX69Q7L0";
    }


    public static void main(String[] args) throws MalformedURLException, ExecutionException, InterruptedException {
        TwitterStreamRuleClient twitterStreamRuleClient = new TwitterStreamRuleClient(
                "https://api.twitter.com/2/tweets/search/stream/rules",
                "https://api.twitter.com/2/tweets/search/stream",
                "YOUR_BEARER_TOKEN",
                Executors.newVirtualThreadPerTaskExecutor()
        );
        twitterStreamRuleClient.addFilterRule("from", "HeLei23302");
        twitterStreamRuleClient.requestAddFilterRule().get();
    }

}
