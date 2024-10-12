package com.helei.tradedatacenter.dto;

import com.alibaba.fastjson.JSONObject;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SubscribeData {

    private JSONObject data;

    private ReentrantLock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();


    public JSONObject getData() {

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


    public void setData(JSONObject result) {
        lock.lock();
        try {
           data = result;
           condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
