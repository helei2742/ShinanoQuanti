package com.helei.tradedatacenter.conventor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.helei.tradedatacenter.entity.KLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.ZoneId;


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
        kLine.setOpenTime(Instant.ofEpochMilli(kjb.getLong("t")).atZone(ZoneId.systemDefault()).toLocalDateTime());
        kLine.setCloseTime(Instant.ofEpochMilli(kjb.getLong("T")).atZone(ZoneId.systemDefault()).toLocalDateTime());
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
        kLine.setOpenTime(Instant.ofEpochMilli(jsonArray.getLong(0)).atZone(ZoneId.systemDefault()).toLocalDateTime());
        kLine.setCloseTime(Instant.ofEpochMilli(jsonArray.getLong(6)).atZone(ZoneId.systemDefault()).toLocalDateTime());
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
