package com.riffsphere.modules;

/**
 * Generic Observable interface.
 * Demonstrates: Observer Pattern, Generics.
 */
public interface Observable<T> {
    void subscribe(T eventType, Observer<T> observer);
    void unsubscribe(T eventType, Observer<T> observer);
    void publish(T event);
}
