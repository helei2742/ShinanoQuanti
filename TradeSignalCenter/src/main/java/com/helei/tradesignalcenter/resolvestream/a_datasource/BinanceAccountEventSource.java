package com.helei.tradesignalcenter.resolvestream.a_datasource;

import com.helei.binanceapi.constants.BinanceApiUrl;
import com.helei.cexapi.client.BinanceAccountMergeClient;
import com.helei.dto.ASKey;
import com.helei.dto.account.AccountLocationConfig;
import com.helei.dto.account.UserInfo;
import com.helei.tradesignalcenter.dto.UserAccountEvent;
import com.helei.tradesignalcenter.service.AccountInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
public class BinanceAccountEventSource extends AccountEventSource {


    private transient BinanceAccountMergeClient accountMergeClient;

    private transient AccountInfoService accountInfoService;


    private final ASKey asKey;

    private final String requestUrl;

    public BinanceAccountEventSource(ASKey asKey, String requestUrl) {
        this.asKey = asKey;
        this.requestUrl = requestUrl;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        accountInfoService = new AccountInfoService();
        accountInfoService.getUid2UserInfo().put("test1", new UserInfo("test1", asKey, List.of("BTCUSDT", "ETHUSDT"), new AccountLocationConfig(0.2, 10, 100)));

        accountMergeClient = new BinanceAccountMergeClient(requestUrl, BinanceApiUrl.WS_ACCOUNT_INFO_STREAM_URL_TEST);
    }


    @Override
    boolean init(SourceContext<UserAccountEvent> sourceContext) throws ExecutionException, InterruptedException {

        Map<String, UserInfo> uid2UserInfo = accountInfoService.getUid2UserInfo();

        List<CompletableFuture<Void>> list = new ArrayList<>();

        AtomicInteger count = new AtomicInteger(0);

        for (Map.Entry<String, UserInfo> entry : uid2UserInfo.entrySet()) {
            CompletableFuture<Void> future = CompletableFuture
                    //每个账户创建连接client，并绑定回调，将事件连同账户uid写入flink source
                    .supplyAsync(
                            () -> {
                                try {
                                    return accountMergeClient.addAccountStream(entry.getValue().getAsKey(), (event) -> {
                                        if (event != null) {
                                            UserAccountEvent userAccountEvent = new UserAccountEvent(entry.getKey(), event);
                                            log.info("收到用户账户事件[{}}", userAccountEvent);
                                            sourceContext.collect(userAccountEvent);
                                        }
                                    });
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            accountMergeClient.getPublishExecutor()
                    )
                    //开始连接
                    .thenAcceptAsync(
                            streamClient -> {
                                Boolean success = null;
                                try {
                                    success = streamClient.startAccountInfoStream().get();
                                } catch (InterruptedException | ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                                if (success) {
                                    log.info("开启账户uid[{}]信息流成功, [{}/{}}", entry.getKey(), count.incrementAndGet(), uid2UserInfo.size());
                                } else {
                                    throw new RuntimeException("开启账户信息推送流出错");
                                }
                            },
                            accountMergeClient.getPublishExecutor()
                    )
                    .whenCompleteAsync(
                            (v, e) -> {
                                if (e != null) {
                                    entry.getValue().getUsable().set(false);
                                    log.error("创建账户信息推送连接发生错误，uid[{}]", entry.getKey(), e);
                                }
                            },
                            accountMergeClient.getPublishExecutor()
                    );
            list.add(future);
        }

        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).get();

        if (count.get() == 0) {
            log.warn("账户信息流建立数量为0");
            return false;
        }
        return true;
    }


    @Override
    void refreshState() {
        accountMergeClient.refreshLink();
    }

}
