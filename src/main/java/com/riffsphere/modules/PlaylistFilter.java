package com.riffsphere.modules;

import com.riffsphere.models.Song;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PlaylistFilter — Functional Interface + Strategy implementations.
 * Demonstrates: Functional Interface, Strategy Pattern, Composite Pattern,
 *               Lambda-friendly design.
 *
 * Usage:
 *   PlaylistFilter filter = new BpmFilter(80, 140).and(new RatingFilter(4.0));
 *   List<Song> filtered = db.searchAdvanced(filter);
 */
@FunctionalInterface
public interface PlaylistFilter {
    boolean test(Song song);

    /** Compose two filters with AND logic (Composite Pattern). */
    default PlaylistFilter and(PlaylistFilter other) {
        return song -> this.test(song) && other.test(song);
    }

    /** Compose two filters with OR logic. */
    default PlaylistFilter or(PlaylistFilter other) {
        return song -> this.test(song) || other.test(song);
    }

    /** Negate this filter. */
    default PlaylistFilter negate() { return song -> !this.test(song); }

    /** A filter that accepts everything. */
    static PlaylistFilter all() { return song -> true; }

    // ── Built-in Concrete Strategies ──────────────────────────────

    /** Filter by BPM range [min, max] inclusive. */
    class BpmFilter implements PlaylistFilter {
        private final int min, max;
        public BpmFilter(int min, int max) { this.min = min; this.max = max; }
        @Override public boolean test(Song s) { return s.getBpm() >= min && s.getBpm() <= max; }
        @Override public String toString()    { return "BPM[" + min + "-" + max + "]"; }
    }

    /** Filter to a specific genre (case-insensitive). */
    class GenreFilter implements PlaylistFilter {
        private final String genre;
        public GenreFilter(String genre) { this.genre = genre.toLowerCase(); }
        @Override public boolean test(Song s) { return s.getGenre().toLowerCase().equals(genre); }
        @Override public String toString()    { return "Genre[" + genre + "]"; }
    }

    /** Filter songs at or above a minimum rating. */
    class RatingFilter implements PlaylistFilter {
        private final double minRating;
        public RatingFilter(double minRating) { this.minRating = minRating; }
        @Override public boolean test(Song s) { return s.getRating() >= minRating; }
        @Override public String toString()    { return "Rating[>=" + minRating + "]"; }
    }

    /** Filter by mood (case-insensitive). */
    class MoodFilter implements PlaylistFilter {
        private final String mood;
        public MoodFilter(String mood) { this.mood = mood.toLowerCase(); }
        @Override public boolean test(Song s) { return s.getMood().equals(mood); }
        @Override public String toString()    { return "Mood[" + mood + "]"; }
    }

    /** Filter by keyword in title or artist. */
    class KeywordFilter implements PlaylistFilter {
        private final String keyword;
        public KeywordFilter(String keyword) { this.keyword = keyword.toLowerCase(); }
        @Override public boolean test(Song s) {
            return s.getTitle().toLowerCase().contains(keyword)
                || s.getArtist().toLowerCase().contains(keyword);
        }
        @Override public String toString() { return "Keyword[" + keyword + "]"; }
    }

    /** AND-chain of multiple filters (Composite Pattern). */
    class CompositeFilter implements PlaylistFilter {
        private final List<PlaylistFilter> filters;
        public CompositeFilter(PlaylistFilter... filters) {
            this.filters = Arrays.asList(filters);
        }
        @Override public boolean test(Song s) {
            return filters.stream().allMatch(f -> f.test(s));
        }
        @Override public String toString() {
            return "Composite" + filters;
        }
    }
}
