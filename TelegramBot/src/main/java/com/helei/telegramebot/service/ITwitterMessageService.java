package com.helei.telegramebot.service;

/**
 * 推特太贵了
 */
@Deprecated
public interface ITwitterMessageService {


    /**
     * 初始化配置文件中的规则
     *
     * @return this
     */
    ITwitterMessageService initConfigFileRule();

    void startListenStream();

    void closeListenStream();
}
