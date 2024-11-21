package com.helei.telegramebot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;

/**
 * <p>
 *
 * </p>
 *
 * @author com.helei
 * @since 2024-11-21
 */
@Getter
@Setter
@TableName("t_chat_wallet")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatWallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 213928773627186442L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * chatId
     */
    @TableField("chat_id")
    private String chatId;

    /**
     * name
     */
    @TableField("name")
    private String name;


    /**
     * 私钥
     */
    @TableField("private_key")
    private String privateKey;

    /**
     * 公匙
     */
    @TableField("public_key")
    private String publicKey;

    /**
     * 密匙
     */
    @TableField("secret_key")
    private String secretKey;

    /**
     * sol数量
     */
    @TableField("solAmount")
    private Double solAmount;

    /**
     * 创建时间
     */
    @TableField("create_datetime")
    private LocalDateTime createDatetime;

    /**
     * 更新时间
     */
    @TableField("update_datetime")
    private LocalDateTime updateDatetime;

    /**
     * 是否可用
     */
    @TableField("is_valid")
    private Boolean isValid;
}
