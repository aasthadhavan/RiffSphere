package com.riffsphere.modules;

/**
 * All application events fired through the EventBus.
 * Demonstrates: Enum with payload, type-safe event system.
 */
public enum AppEvent {
    SONG_PLAYED,
    MOOD_CHANGED,
    PLAYLIST_CREATED,
    PLAYLIST_DELETED,
    PLAYLIST_UPDATED,
    FAVORITE_ADDED,
    FAVORITE_REMOVED,
    USER_LOGGED_IN,
    USER_LOGGED_OUT,
    PERSONALITY_UPDATED,
    STATS_UPDATED,
    SONG_RATED
}
