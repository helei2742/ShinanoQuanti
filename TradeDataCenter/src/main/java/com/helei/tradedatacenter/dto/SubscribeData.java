package com.helei.tradedatacenter.dto;

import com.alibaba.fastjson.JSONObject;
import com.helei.tradedatacenter.conventor.KLineMapper;
import com.helei.tradedatacenter.entity.KLine;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SubscribeData<T> implements Serializable {

    private T data;

    private ReentrantLock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();


    public T getData() throws InterruptedException {
        lock.lock();
        try {
            while (data == null) {
                condition.await();
            }

            return data;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }


    public void setData(T result) {
        lock.lock();
        try {
           data = result;
           condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
