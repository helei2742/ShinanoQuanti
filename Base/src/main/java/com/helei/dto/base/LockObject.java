package com.helei.dto.base;

import java.util.concurrent.locks.ReentrantLock;


public abstract class LockObject {

    private final ReentrantLock lock = new ReentrantLock();

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }
}
