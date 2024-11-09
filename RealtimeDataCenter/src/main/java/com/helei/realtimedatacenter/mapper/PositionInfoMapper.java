package com.helei.realtimedatacenter.mapper;

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
            @Mapping(source = "position", target = "positionAmt"),
            @Mapping(source = "entryPrice", target = "entryPrice"),
            @Mapping(source = "balanceEqualPrice", target = "balanceEqualPrice"),
            @Mapping(source = "countProfitOrLoss", target = "countProfitOrLoss"),
            @Mapping(source = "unrealizedProfitOrLoss", target = "unrealizedProfit"),
            @Mapping(source = "marginMode", target = "marginMode"),
            @Mapping(source = "bail", target = "maintMargin"),
            @Mapping(source = "positionSide", target = "positionSide")
    })
    PositionInfo convertFromPositionChangeInfo(BalancePositionUpdateEvent.PositionChangeInfo positionChangeInfo);

    List<PositionInfo> convertFromPositionChangeInfoList(List<BalancePositionUpdateEvent.PositionChangeInfo> positionChangeInfoList);
}
