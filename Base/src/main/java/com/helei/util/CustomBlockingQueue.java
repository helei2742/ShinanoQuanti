package com.helei.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CustomBlockingQueue<E> {

    private final ArrayBlockingQueue<E> queue;

    public CustomBlockingQueue(int capacity) {

        queue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(E e) {
        // 如果队列满了，先移除头部元素
        if (queue.size() == queue.remainingCapacity()) {
            queue.poll();
        }
        // 然后添加新元素
        return queue.offer(e);
    }

    public E take() throws InterruptedException {
        return queue.take();
    }

    public E poll(long waitTime, TimeUnit unit) throws InterruptedException {
        return queue.poll(waitTime, unit);
    }
}
