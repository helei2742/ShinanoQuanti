package com.helei.dto.account;


import com.helei.dto.base.LockObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPositionInfo extends LockObject implements Serializable {


    /**
     * 账户id
     */
    private long accountId;

    /**
     * 更新时间
     */
    private long updateTime;

    /**
     * 仓位信息
     */
    private final ConcurrentHashMap<String, PositionInfo> positions = new ConcurrentHashMap<>();


    /**
     * 更新仓位信息
     * @param positionInfos positionInfos
     */
    public void updatePositionInfos(List<PositionInfo> positionInfos) {
        positionInfos.forEach(positionInfo -> {
            if (positionInfo.getPositionAmt().doubleValue() == 0) {
                return;
            }
            positions.put(positionInfo.getSymbol(), positionInfo);
        });
    }
}
