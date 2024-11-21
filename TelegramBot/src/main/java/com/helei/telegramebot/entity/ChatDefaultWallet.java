package com.helei.telegramebot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@TableName("t_chat_default_wallet")
@AllArgsConstructor
@NoArgsConstructor
public class ChatDefaultWallet implements Serializable {

    @Serial
    private static final long serialVersionUID = 21321212121212121L;

    /**
     * chatId
     */
    @TableId(value = "chat_id", type = IdType.INPUT)
    private String chatId;

    /**
     * 钱包公匙（地址）
     */
    @TableField("public_key")
    private String publicKey;
}
