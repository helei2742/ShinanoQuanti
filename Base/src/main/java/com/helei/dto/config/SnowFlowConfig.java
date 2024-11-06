package com.helei.dto.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnowFlowConfig {

    /**
     * 数据中心id
     */
    private int datacenter_id = 0;

    /**
     * 机器id
     */
    private int machine_id = 0;
}
