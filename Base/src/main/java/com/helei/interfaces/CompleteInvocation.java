package com.helei.interfaces;



public interface CompleteInvocation<T> {

    void success(T t);

    void fail(T t, String errorMsg);

    void finish();
}
