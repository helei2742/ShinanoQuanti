package com.helei.dto.account;


import com.helei.dto.ASKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    /**
     * 用户id
     */
    private String id;

    /**
     * 验证key
     */
    private ASKey asKey;

    /**
     * 是否可用
     */
    private final AtomicBoolean usable = new AtomicBoolean(false);

    /**
     * 订阅的交易对
     */
    private List<String> subscribeSymbol;

    /**
     * 账户仓位设置
     */
    private AccountLocationConfig accountLocationConfig;
}
