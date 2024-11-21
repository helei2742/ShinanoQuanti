CREATE TABLE `t_chat_wallet`
(
    `id`              bigint                                                        NOT NULL AUTO_INCREMENT,
    `chat_id`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'chatId',
    `name`            varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `private_key`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '私钥',
    `public_key`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公匙',
    `secret_key`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密匙\r\n',
    `solAmount` double DEFAULT NULL COMMENT 'sol数量',
    `create_datetime` datetime                                                      DEFAULT NULL COMMENT '创建时间',
    `update_datetime` datetime                                                      DEFAULT NULL COMMENT '更新时间',
    `is_valid`        tinyint(1) DEFAULT NULL COMMENT '是否可用',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `t_chat_default_wallet`
(
    `chat_id`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'chatId',
    `public_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '钱包公匙（地址）',
    PRIMARY KEY (`chat_id` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

