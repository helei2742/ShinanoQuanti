package com.helei.binanceapi.util;


import com.alibaba.fastjson.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * 处理secret key签名相关
 */
public class SignatureUtil {

    /**
     * 计算signature
     *
     * @param secretKey secretKey
     * @param params    params
     * @return signature
     */
    public static String signatureHMAC(String secretKey, JSONObject params) throws Exception {
        StringBuilder payload = new StringBuilder();

        params.keySet().stream().sorted().forEach(key -> {
            payload.append(key).append("=").append(params.get(key)).append("&");
        });

        if (!payload.isEmpty()) {
            payload.deleteCharAt(payload.length() - 1);
        }

        return hmac256(secretKey, payload.toString());
    }

    // 辅助方法：将 byte[] 转换为十六进制字符串
    public static String hmac256(String secretKey, String data) throws Exception {
        // 创建 HMAC SHA-256 密钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        // 获取 HMAC 实例
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);

        // 计算 HMAC
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        // 将结果转换为十六进制字符串
        return new BigInteger(1, hmacBytes).toString(16);
    }

    // 辅助方法：将 byte[] 转换为十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws Exception {
        JSONObject jb = new JSONObject();

        jb.put("type", "LIMIT");
        jb.put("symbol", "BTCUSDT");
        jb.put("side", "SELL");
        jb.put("timeInForce", "GTC");
        jb.put("quantity", "0.01000000");
        jb.put("price", "52000.00");
        jb.put("newOrderRespType", "ACK");
        jb.put("recvWindow", 100);
        jb.put("timestamp", "1645423376532");
        jb.put("apiKey", "vmPUZE6mv9SD5VNHk4HlWFsOr6aKE2sass0MuIgwCIPy6utIco14y7Ju91duEh8A");

        String signature = signatureHMAC("NhqPtmdSJYdKjVHjA7PZj4Mge3R5YNiP1e3UZjInClVN65XAbvqqM6A7H5fATj0j", jb);

        System.out.println(signature);
    }
}
