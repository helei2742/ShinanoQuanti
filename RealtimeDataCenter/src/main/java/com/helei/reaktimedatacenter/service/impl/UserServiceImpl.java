package com.helei.reaktimedatacenter.service.impl;

import com.helei.constants.RunEnv;
import com.helei.constants.TradeType;
import com.helei.dto.ASKey;
import com.helei.dto.account.UserAccountInfo;
import com.helei.dto.account.UserInfo;
import com.helei.reaktimedatacenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class UserServiceImpl implements UserService {


    @Override
    public List<UserInfo> queryAll() {
        List<UserInfo> list = new ArrayList<>();
        //TODO 查数据库

        UserInfo u_contract_test_net_account = UserInfo.builder()
                .id(1)
                .username("合约测试网账号")
                .password("123456")
                .accountInfos(List.of(
                        UserAccountInfo
                                .builder()
                                .id(1)
                                .userId(1)
                                .asKey(new ASKey("b252246c6c6e81b64b8ff52caf6b8f37471187b1b9086399e27f6911242cbc66", "a4ed1b1addad2a49d13e08644f0cc8fc02a5c14c3511d374eac4e37763cadf5f"))
                                .subscribeSymbol(List.of("btcusdt", "ethusdt", "solusdt"))
                                .runEnv(RunEnv.TEST_NET)
                                .tradeType(TradeType.CONTRACT)
                                .build()
                ))
                .build();
        UserInfo spot_test_net_account = UserInfo.builder()
                .id(2)
                .username("现货测试网账号")
                .password("123456")
                .accountInfos(List.of(
                        UserAccountInfo
                                .builder()
                                .id(2)
                                .userId(1)
                                .subscribeSymbol(List.of("btcusdt", "ethusdt", "solusdt"))
                                .asKey(new ASKey("1JIhkPyK07xadG9x8hIwqitN95MgpypPzA4b6TLraTonRnJ8BBJQlaO2iL9tPH0Y", "t84TYFR1zieMGncbw3kYq4zAPLxIJHJeMdD8V0FMKxij9fApojV6bhbDpyyjNDWt"))
                                .runEnv(RunEnv.TEST_NET)
                                .tradeType(TradeType.CONTRACT)
                                .build()
                ))
                .build();

        UserInfo binance_account = UserInfo.builder()
                .id(3)
                .username("正式网账号")
                .password("123456")
                .accountInfos(List.of(
                        UserAccountInfo
                                .builder()
                                .id(3)
                                .userId(1)
                                .subscribeSymbol(List.of("btcusdt", "ethusdt", "solusdt"))
                                .asKey(new ASKey("TUFsFL4YrBsR4fnBqgewxiGfL3Su5L9plcjZuyRO3cq6M1yuwV3eiNX1LcMamYxz", "YsLzVacYo8eOGlZZ7RjznyWVjPHltIXzZJz2BrggCmCUDcW75FyFEv0uKyLBVAuU"))
                                .runEnv(RunEnv.TEST_NET)
                                .tradeType(TradeType.CONTRACT)
                                .build()
                ))
                .build();


        list.add(u_contract_test_net_account);
        list.add(spot_test_net_account);
        list.add(binance_account);
        return list;
    }
}
