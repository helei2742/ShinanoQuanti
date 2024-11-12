package com.helei.tradeapplication.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.helei.constants.CEXType;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.awt.*;
        import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {

    private static final Pattern runEnvPattern = Pattern.compile("%env\\((.*?)\\)%");
    private static final Pattern tradeTypePattern = Pattern.compile("%env\\((.*?)\\)%");
    private static final Pattern cexTypePattern = Pattern.compile("%env\\((.*?)\\)%");


    /**
     * 使用mp做添加操作时候，这个方法执行
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        //设置属性值
        this.setFieldValByName("createdDatetime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updatedDatetime", LocalDateTime.now(), metaObject);
    }

    /**
     * 使用mp做修改操作时候，这个方法执行
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updatedDatetime", LocalDateTime.now(), metaObject);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 配置查询动态表名的拦截器，使用时先在TableNameHelper设置要查询的动态表名，然后直接执行mybatisplus的查询方法。注意赋予的动态表名只在一次查询中有效
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        dynamicTableNameInnerInterceptor.setTableNameHandler(this::fixTableName);
        mybatisPlusInterceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);

//        分页配置
        mybatisPlusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        return mybatisPlusInterceptor;
    }


    /**
     * 动态修改t_trade_order 订单目标表的后缀，指向不同环境的订单表
     * @param sql   sql
     * @param oldTableName 原始table name
     * @return  new table name
     */
    private String fixTableName(String sql, String oldTableName) {
        if (!oldTableName.startsWith("t_trade_order")) {
            return oldTableName;
        }
        Matcher envMatch = runEnvPattern.matcher(sql);
        String env = null;
        if (envMatch.find()) {
            env = envMatch.group(1);
        } else {
            return oldTableName;
        }

        Matcher tradeTypeMatch = tradeTypePattern.matcher(sql);
        String tradeType = null;
        if (tradeTypeMatch.find()) {
            tradeType = tradeTypeMatch.group(1);
        } else {
            return oldTableName;
        }

        Matcher cexTypeMatch = cexTypePattern.matcher(sql);
        String cexType = null;
        if (cexTypeMatch.find()) {
            env = cexTypeMatch.group(1);
        } else {
            return oldTableName;
        }


        return oldTableName + "_" + cexType + "_" + env + "_" +tradeType;
    }
}

