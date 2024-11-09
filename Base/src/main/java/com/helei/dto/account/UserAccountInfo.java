package com.helei.dto.account;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户账户信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccountInfo {

    /**
     * 账户id
     */
    private long id;

    /**
     * 用户id
     */
    private long userId;

    /**
     * 账户的静态数据
     */
    private UserAccountStaticInfo userAccountStaticInfo;

    /**
     * 账户的实时数据
     */
    private UserAccountRealTimeInfo userAccountRealTimeInfo;

}
