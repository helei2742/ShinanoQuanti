package com.helei.tradedatacenter.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * WebSocket里发送请求的格式
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WebSocketCommand {

    /**
     * 请求的类型
     */
    private String method;

    /**
     * 参数
     */
    private List<String> params;

    /**
     * id
     */
    private String id;
}
