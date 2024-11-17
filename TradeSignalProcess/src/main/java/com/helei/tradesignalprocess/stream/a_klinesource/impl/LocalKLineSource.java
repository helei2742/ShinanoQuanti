package com.helei.tradesignalprocess.stream.a_klinesource.impl;

import com.alibaba.fastjson.JSONObject;
import com.helei.dto.trade.KLine;
import com.helei.tradesignalprocess.stream.a_klinesource.KLineHisAndRTSource;
import org.apache.flink.configuration.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class LocalKLineSource extends KLineHisAndRTSource {

    private final String filePath;

    private BufferedReader reader;

    public LocalKLineSource(String filePath) {
        super("", Set.of(), System.currentTimeMillis());
        this.filePath = filePath;
    }

    @Override
    protected void onOpen(Configuration parameters) throws Exception {
        reader = new BufferedReader(new FileReader(filePath));
    }

    @Override
    protected void loadDataInBuffer(BlockingQueue<KLine> buffer) {
        reader.lines().forEach(line -> {
            try {
                buffer.put(JSONObject.parseObject(line, KLine.class));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}

