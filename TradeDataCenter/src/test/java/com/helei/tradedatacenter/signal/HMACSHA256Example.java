package com.helei.tradedatacenter.signal;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class HMACSHA256Example {

    public static void main(String[] args) {
        try {
            // 示例密钥和有效负载
            String secretKey = "y58vInhTIfk5qIXGzxQcgzerghMAoqhzWzAMqc72BdyhVOpBPoJ41KoQmh8ZEYCw";
            String data = "apiKey=hpNdqT88HNB8B8sIl4ahGGJtCWBb4Cd6E7nOEeCOMAIfhBnSoNOmZ7vWxAbkik8N&omitZeroBalances=true&timestamp=1729302452380";

            // 计算 HMAC
            String hmac = hmac256(secretKey, data);
            System.out.println("HMAC SHA256 (hex): " + hmac);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // HMAC-SHA256 方法
    public static String hmac256(String secretKey, String data) throws Exception {
        // 创建 HMAC SHA-256 密钥
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.US_ASCII), "HmacSHA256");

        // 获取 HMAC 实例
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);

        // 计算 HMAC
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.US_ASCII));

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
}
