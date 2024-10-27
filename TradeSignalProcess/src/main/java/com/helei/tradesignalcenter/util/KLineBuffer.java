package com.helei.tradesignalcenter.util;

import com.helei.dto.KLine;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.ArrayBlockingQueue;

public class KLineBuffer extends ArrayBlockingQueue<KLine> implements Serializable {
    @Serial
    private static final long serialVersionUID = 99999L; // 显式声明 serialVersionUID


    public KLineBuffer(int capacity) {
        super(capacity);
    }
}
