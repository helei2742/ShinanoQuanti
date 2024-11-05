package com.helei.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaConfig {

    private String bootstrap_servers;

    private String group_id = "default_group";

    /**
     * kafka写入实时k线时设置几个分区
     */
    private int kafka_num_partitions;

    /**
     * kafka的副本个数
     */
    private short kafka_replication_factor;

}
