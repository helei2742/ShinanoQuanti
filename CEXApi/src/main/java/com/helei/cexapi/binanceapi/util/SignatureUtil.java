package com.helei.cexapi.binanceapi.util;


        import com.alibaba.fastjson.JSONObject;

        import javax.crypto.Mac;
        import javax.crypto.spec.SecretKeySpec;
        import java.nio.charset.StandardCharsets;
        import java.security.InvalidKeyException;
        import java.security.NoSuchAlgorithmException;
        import java.util.Base64;

/**
 * 处理secret key签名相关
 */
public class SignatureUtil {

    private final static Mac sha256_HMAC;

    static {
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算signature
     * @param secretKey secretKey
     * @param params params
     * @return signature
     */
    public static String signatureHMAC(String secretKey, JSONObject params) throws InvalidKeyException {
        StringBuilder payload = new StringBuilder();

        params.keySet().stream().sorted().forEach(key->{
            payload.append(key).append("=").append(params.get(key)).append("&");
        });

        if (!payload.isEmpty()) {
            payload.deleteCharAt(payload.length()-1);
        }
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        sha256_HMAC.init(secretKeySpec);

        byte[] hash = sha256_HMAC.doFinal(payload.toString().getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hash);
    }
}
