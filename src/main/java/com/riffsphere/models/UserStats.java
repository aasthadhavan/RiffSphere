package com.riffsphere.models;

import java.util.*;

/**
 * Aggregate statistics for a single User.
 * Updated by the User model on every play/mood-change.
 * Demonstrates: Encapsulation, Aggregation, Single-Responsibility.
 */
public class UserStats {

    private int    totalPlays     = 0;
    private String topGenre       = "—";
    private String favoriteMood   = "—";
    private double avgRatingGiven = 0.0;

    private final Map<String, Integer> genreCounts = new LinkedHashMap<>();
    private final Map<String, Integer> moodCounts  = new LinkedHashMap<>();
    private final List<String>         moodHistory = new ArrayList<>();

    public UserStats() {}

    // ── Recording ──────────────────────────────────────────────────
    public void recordPlay(Song song) {
        if (song == null) return;
        totalPlays++;
        genreCounts.merge(song.getGenre(), 1, Integer::sum);
        moodCounts.merge(song.getMood(),   1, Integer::sum);
        topGenre     = maxKey(genreCounts);
        favoriteMood = maxKey(moodCounts);
    }

    public void recordMood(String mood) {
        if (mood == null || mood.isBlank()) return;
        moodHistory.add(0, mood.toLowerCase());
        if (moodHistory.size() > 10) moodHistory.remove(moodHistory.size() - 1);
        moodCounts.merge(mood.toLowerCase(), 1, Integer::sum);
        favoriteMood = maxKey(moodCounts);
    }

    public void updateAvgRating(Map<String, Double> ratings) {
        if (ratings == null || ratings.isEmpty()) { avgRatingGiven = 0; return; }
        avgRatingGiven = ratings.values().stream()
                               .mapToDouble(Double::doubleValue)
                               .average().orElse(0);
    }

    // ── Accessors ──────────────────────────────────────────────────
    public int    getTotalPlays()   { return totalPlays; }
    public String getTopGenre()     { return topGenre; }
    public String getFavoriteMood() { return favoriteMood; }
    public double getAvgRating()    { return avgRatingGiven; }

    public Map<String, Integer> getGenreCounts() { return Collections.unmodifiableMap(genreCounts); }
    public Map<String, Integer> getMoodCounts()  { return Collections.unmodifiableMap(moodCounts);  }
    public List<String>         getMoodHistory() { return Collections.unmodifiableList(moodHistory); }

    // ── Private helpers ────────────────────────────────────────────
    private static String maxKey(Map<String, Integer> map) {
        return map.entrySet().stream()
                  .max(Map.Entry.comparingByValue())
                  .map(Map.Entry::getKey)
                  .orElse("—");
    }
}
