package com.helei.realtimedatacenter.mapper;

import com.helei.binanceapi.dto.accountevent.BalancePositionUpdateEvent;
import com.helei.dto.account.BalanceInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

//@Mapper
@Deprecated
public interface BalanceInfoMapper {

    BalanceInfoMapper INSTANCE = Mappers.getMapper(BalanceInfoMapper.class);

    @Mappings({
            @Mapping(source = "asset", target = "asset"),
            @Mapping(source = "bailRemoveWalletBalance", target = "free"),
            @Mapping(target = "locked", expression = "java(changeChangeInfo.getWalletBalance() - changeChangeInfo.getBailRemoveWalletBalance())")
    })
    BalanceInfo convertFromBalanceChangeInfo(BalancePositionUpdateEvent.BalanceChangeInfo changeChangeInfo);

    List<BalanceInfo> convertFromBalanceChangeInfoList(List<BalancePositionUpdateEvent.BalanceChangeInfo> changeChangeInfoList);
}
