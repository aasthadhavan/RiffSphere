package com.riffsphere.modules;

/**
 * Generic Observer interface.
 * Demonstrates: Observer Pattern, Generics.
 */
public interface Observer<T> {
    void onEvent(T event);
}
