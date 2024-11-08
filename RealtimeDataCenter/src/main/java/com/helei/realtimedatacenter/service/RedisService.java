package com.helei.realtimedatacenter.service;

public interface RedisService {


    void saveKeyValue(String key, Object value);

    void saveHashKeyValue(String key, String hashKey, String value);
}
