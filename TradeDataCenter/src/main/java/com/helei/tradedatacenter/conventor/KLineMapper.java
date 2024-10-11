package com.helei.tradedatacenter.conventor;

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
        KLine kLine = new KLine();
        kLine.setOpenTime(Instant.ofEpochMilli(jsonObject.getLong("t")).atZone(ZoneId.systemDefault()).toLocalDateTime());
        kLine.setCloseTime(Instant.ofEpochMilli(jsonObject.getLong("T")).atZone(ZoneId.systemDefault()).toLocalDateTime());
        kLine.setSymbol(jsonObject.getString("s"));
        kLine.setHigh(jsonObject.getDouble("h"));
        kLine.setLow(jsonObject.getDouble("l"));
        kLine.setOpen(jsonObject.getDouble("o"));
        kLine.setClose(jsonObject.getDouble("c"));
        kLine.setVolume(jsonObject.getDouble("v"));
        kLine.setEnd(jsonObject.getBoolean("x"));
        return kLine;
    }
}
