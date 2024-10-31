package com.helei.dto.account;


import cn.hutool.core.collection.ConcurrentHashSet;
import com.helei.dto.LockObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountPositionInfo extends LockObject {


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
    private final ConcurrentHashSet<PositionInfo> positions = new ConcurrentHashSet<>();


    /**
     * 更新仓位信息
     * @param positionInfos positionInfos
     */
    public synchronized void updatePositionInfos(List<PositionInfo> positionInfos) {
        this.positions.clear();
        this.positions.addAll(positionInfos);
    }
}
