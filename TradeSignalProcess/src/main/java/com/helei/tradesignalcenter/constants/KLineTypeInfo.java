package com.helei.tradesignalcenter.constants;

import com.helei.dto.KLine;
import com.helei.tradesignalcenter.serialization.KLineSerializer;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.TypeSerializer;


public class KLineTypeInfo extends TypeInformation<KLine> {

    @Override
    public boolean isBasicType() {
        return false;  // `KLine` 不是基本类型
    }

    @Override
    public boolean isTupleType() {
        return false;  // `KLine` 不是元组类型
    }

    @Override
    public int getArity() {
        return 11;  // `KLine` 中的字段数量
    }

    @Override
    public int getTotalFields() {
        return 11;  // `KLine` 中的总字段数
    }

    @Override
    public Class<KLine> getTypeClass() {
        return KLine.class;  // 返回 `KLine` 类
    }

    @Override
    public boolean isKeyType() {
        return false;  // 设置 `KLine` 不是 key 类型，具体根据需求调整
    }

    @Override
    public TypeSerializer<KLine> createSerializer(org.apache.flink.api.common.ExecutionConfig config) {
        return new KLineSerializer();  // 创建 `KLine` 的序列化器实例
    }

    @Override
    public String toString() {
        return "KLineTypeInfo";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof KLineTypeInfo;
    }

    @Override
    public int hashCode() {
        return KLine.class.hashCode();
    }

    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof KLineTypeInfo;
    }
}
