package com.helei.binanceapi.supporter;


import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

//TODO ip 请求限制尚未开发
/**
 * 实时计算ip weight
 */
@Slf4j
public class IpWeightSupporter {

    private final String ip;

    /**
     * 当前的ip weight
     */
    private final AtomicInteger ipWeight;

    public IpWeightSupporter(String ip) {
        this.ip = ip;
        this.ipWeight = new AtomicInteger(0);
    }

    /**
     * 提交ip权重，返回boolean代表能否不受限制的安全执行
     * @param ipWeight 请求的权重
     * @return 是否建议执行
     */
    public boolean submitIpWeight(int ipWeight) {
        //TODO 添加逻辑，判断是否能够执行
        this.ipWeight.addAndGet(ipWeight);
        return true;
    }

    /**
     * 获取当前的 ip weight
     * @return ip weight
     */
    public Integer currentWeight() {
        return ipWeight.get();
    }
}

