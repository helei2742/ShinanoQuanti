package com.helei.dto.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnowFlowConfig implements Serializable {

    /**
     * 数据中心id
     */
    private int datacenter_id = 0;

    /**
     * 机器id
     */
    private int machine_id = 0;
}
