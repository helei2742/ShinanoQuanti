package com.helei.twiterstream.dto;

import lombok.Data;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * 推特的流的过滤规则
 */
@Data
public class TwitterFilterRule {

    public final List<JSONObject> add = new ArrayList<>();


    public void addRule(String rule) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("value", rule);
        add.add(jsonObject);
    }
}
