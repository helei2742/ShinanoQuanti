package com.helei.tradesignalcenter.dto;

import com.helei.binanceapi.dto.accountevent.AccountEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAccountEvent {

    private String uid;

    private AccountEvent event;
}
