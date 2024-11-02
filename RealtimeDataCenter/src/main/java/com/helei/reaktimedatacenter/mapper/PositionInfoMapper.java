package com.helei.reaktimedatacenter.mapper;

import com.helei.binanceapi.dto.accountevent.BalancePositionUpdateEvent;
import com.helei.dto.account.PositionInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface PositionInfoMapper {

    PositionInfoMapper INSTANCE = Mappers.getMapper(PositionInfoMapper.class);


    @Mappings({
            @Mapping(source = "symbol", target = "symbol"),
            @Mapping(source = "position", target = "position"),
            @Mapping(source = "enterPosition", target = "enterPosition"),
            @Mapping(source = "balanceEqualPrice", target = "balanceEqualPrice"),
            @Mapping(source = "countProfitOrLoss", target = "countProfitOrLoss"),
            @Mapping(source = "unrealizedProfitOrLoss", target = "unrealizedProfitOrLoss"),
            @Mapping(source = "marginMode", target = "marginMode"),
            @Mapping(source = "bail", target = "bail"),
            @Mapping(source = "positionSide", target = "positionSide")
    })
    PositionInfo convertFromPositionChangeInfo(BalancePositionUpdateEvent.PositionChangeInfo positionChangeInfo);

    List<PositionInfo> convertFromPositionChangeInfoList(List<BalancePositionUpdateEvent.PositionChangeInfo> positionChangeInfoList);
}
