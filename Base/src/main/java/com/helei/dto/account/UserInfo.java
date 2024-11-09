package com.helei.dto.account;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfo {

    /**
     * 用户id
     */
    private long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;


    /**
     * 名下账户列表
     */
    private Set<Long> accountIds;

    /**
     * 用户的账户信息
     */
    private List<UserAccountInfo> accountInfos;
}
