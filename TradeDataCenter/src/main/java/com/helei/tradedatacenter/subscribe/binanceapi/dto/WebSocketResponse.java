package com.helei.tradedatacenter.subscribe.binanceapi.dto;

        import com.alibaba.fastjson.JSON;
        import lombok.AllArgsConstructor;
        import lombok.Data;
        import lombok.EqualsAndHashCode;
        import lombok.NoArgsConstructor;

        import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class WebSocketResponse {

    /**
     * 请求的id
     */
    private String id;

    /**
     * 相应状态
     */
    private Integer status;

    /**
     * 响应内容
     */
    private JSON result;

    /**
     * 错误描述
     */
    private String error;

    /**
     * 限速状态
     */
    private List<RateLimit> rateLimits;
}
