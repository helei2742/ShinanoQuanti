package com.helei.tradesignalcenter.conventor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.dto.KLine;


//@Mapper
public class KLineMapper {
//    KLineMapper INSTANCE = Mappers.getMapper(KLineMapper.class);

//    @Mapping(source = "jsonObject", target = "kline", qualifiedByName = "jsonToKLine")
//    KLine jsonObj2KLine(JSONObject jsonObject);

    // 自定义映射方法
//    @Named("jsonToKLine")
    public static  KLine mapJsonToKLine(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        JSONObject kjb = jsonObject.getJSONObject("data").getJSONObject("k");

        KLine kLine = new KLine();
        kLine.setOpenTime(kjb.getLong("t"));
        kLine.setCloseTime(kjb.getLong("T"));
        kLine.setSymbol(kjb.getString("s"));
        kLine.setHigh(kjb.getDouble("h"));
        kLine.setLow(kjb.getDouble("l"));
        kLine.setOpen(kjb.getDouble("o"));
        kLine.setClose(kjb.getDouble("c"));
        kLine.setVolume(kjb.getDouble("v"));
        kLine.setEnd(kjb.getBoolean("x"));
        return kLine;
    }

    public static  KLine mapJsonArrToKLine(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        KLine kLine = new KLine();
        kLine.setOpenTime(jsonArray.getLong(0));
        kLine.setCloseTime(jsonArray.getLong(6));
//        kLine.setSymbol(kjb.getString("s"));
        kLine.setHigh(jsonArray.getDouble(2));
        kLine.setLow(jsonArray.getDouble(3));
        kLine.setOpen(jsonArray.getDouble(1));
        kLine.setClose(jsonArray.getDouble(4));
        kLine.setVolume(jsonArray.getDouble(5));
        kLine.setEnd(true);
        return kLine;
    }
}
