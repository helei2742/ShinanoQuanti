
package com.helei.cexapi.binanceapi.dto;


        import lombok.Data;

@Data
public class ASKey {

    /**
     * apikey，websocket时防请求参数体里，http时放在请求头
     */
    private String apiKey;

    /**
     * secretKey，用于加密算法计算签名，放在请求参数里
     */
    private String secretKey;
}
