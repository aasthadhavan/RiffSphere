package com.riffsphere.models;

/**
 * Abstract root of the media hierarchy.
 * Both Song (and any future Podcast / AudioBook) extend this.
 * Demonstrates: Abstraction, Encapsulation, Template-Method pattern.
 */
public abstract class MediaItem {

    protected final String id;
    protected       String title;

    protected MediaItem(String id, String title) {
        if (id    == null || id.isBlank())    throw new IllegalArgumentException("MediaItem id cannot be blank");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("MediaItem title cannot be blank");
        this.id    = id;
        this.title = title;
    }

    // ── Template accessors ──────────────────────────────────────────
    public final  String getId()        { return id; }
    public        String getTitle()     { return title; }
    public        void   setTitle(String t) { if (t != null && !t.isBlank()) this.title = t; }

    // ── Abstract protocol ───────────────────────────────────────────
    /** Human-readable one-liner for display. */
    public abstract String getDisplayInfo();
    /** CSV serialisation used by FileStorage. */
    public abstract String toCsv();

    // ── Equality is identity (by id) ─────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaItem)) return false;
        return id.equals(((MediaItem) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() { return getDisplayInfo(); }
}
