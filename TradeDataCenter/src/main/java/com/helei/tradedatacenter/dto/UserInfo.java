package com.helei.tradedatacenter.dto;

import com.helei.cexapi.binanceapi.dto.ASKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
     * 订阅的交易对
     */
    private List<String> subscribeSymbol;

    /**
     * 账户仓位设置
     */
    private AccountLocationConfig accountLocationConfig;
}
