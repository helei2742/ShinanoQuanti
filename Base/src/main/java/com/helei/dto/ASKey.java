package com.helei.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ASKey implements Serializable {

    public static final ASKey EMPTY_ASKEY = new ASKey();

    /**
     * apikey，websocket时防请求参数体里，http时放在请求头
     */
    private String apiKey;

    /**
     * secretKey，用于加密算法计算签名，放在请求参数里
     */
    private String secretKey;
}
