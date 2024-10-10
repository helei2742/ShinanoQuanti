package com.helei.cexapi.netty;

import io.netty.util.AttributeKey;

public class NettyConstants {

    /**
     * 一朕的最大长度
     */
    public final static int MAX_FRAME_LENGTH = 1024;


    /**
     * 放在netty channel 里的 client id 的 key
     */
    public static final AttributeKey<String> ATTRIBUTE_KEY = AttributeKey.valueOf("clientId");
}
