package com.helei.solanarpc.dto;

import com.helei.solanarpc.constants.SolanaWSRequestType;
import com.helei.solanarpc.support.SolanaEventInvocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SolanaWSRequestContext {

    private String address;

    private Long requestId;

    private Long subscription;

    private SolanaWSRequestType requestType;

    private SolanaEventInvocation invocation;
}
