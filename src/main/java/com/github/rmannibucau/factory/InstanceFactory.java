package com.github.rmannibucau.factory;

public interface InstanceFactory {
    <T> T instance(Class<T> type);
}
