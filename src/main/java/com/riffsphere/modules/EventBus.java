package com.riffsphere.modules;

import java.util.*;

/**
 * Singleton EventBus — central publish/subscribe hub.
 * GUI panels subscribe; backend services fire events.
 * Demonstrates: Singleton Pattern, Observer Pattern, Generics.
 *
 * Usage:
 *   EventBus.getInstance().subscribe(AppEvent.MOOD_CHANGED, e -> panel.refresh());
 *   EventBus.getInstance().publish(AppEvent.MOOD_CHANGED);
 */
public class EventBus implements Observable<AppEvent> {

    private static EventBus instance;
    private final Map<AppEvent, List<Observer<AppEvent>>> listeners = new EnumMap<>(AppEvent.class);

    private EventBus() {}

    public static synchronized EventBus getInstance() {
        if (instance == null) instance = new EventBus();
        return instance;
    }

    @Override
    public synchronized void subscribe(AppEvent eventType, Observer<AppEvent> observer) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(observer);
    }

    @Override
    public synchronized void unsubscribe(AppEvent eventType, Observer<AppEvent> observer) {
        List<Observer<AppEvent>> obs = listeners.get(eventType);
        if (obs != null) obs.remove(observer);
    }

    @Override
    public void publish(AppEvent event) {
        List<Observer<AppEvent>> obs = listeners.getOrDefault(event, Collections.emptyList());
        new ArrayList<>(obs).forEach(o -> o.onEvent(event));
    }

    /** Convenience: subscribe one observer to multiple events at once. */
    public void subscribeAll(Observer<AppEvent> observer, AppEvent... events) {
        for (AppEvent e : events) subscribe(e, observer);
    }

    /** Clear all listeners (on logout / restart). */
    public synchronized void reset() { listeners.clear(); }
}
