package com.riffsphere.modules;

import com.riffsphere.models.*;
import java.util.*;
import java.util.stream.Collectors;

// ── Recommendation Strategy interface ─────────────────────────────────────
interface RecommendationStrategy {
    List<Song> recommend(User user, SongDatabase db, int limit);
}

// ── Concrete Strategies ───────────────────────────────────────────────────
class MoodBasedRecommender implements RecommendationStrategy {
    @Override
    public List<Song> recommend(User user, SongDatabase db, int limit) {
        List<Song> pool = new ArrayList<>(db.getByMood(user.getCurrentMood()));
        if (pool.isEmpty()) pool = new ArrayList<>(db.getAllSongs());
        Collections.sort(pool);
        int topN = Math.min(pool.size(), limit * 2);
        List<Song> top = new ArrayList<>(pool.subList(0, topN));
        Collections.shuffle(top);
        return top.subList(0, Math.min(top.size(), limit));
    }
}

class PersonalityBasedRecommender implements RecommendationStrategy {
    @Override
    public List<Song> recommend(User user, SongDatabase db, int limit) {
        List<Song> pool = new ArrayList<>(db.getByPersonality(user.getPersonalityType()));
        if (pool.isEmpty()) pool = new ArrayList<>(db.getAllSongs());
        Collections.sort(pool);
        return pool.subList(0, Math.min(pool.size(), limit));
    }
}

class HybridRecommender implements RecommendationStrategy {
    private final MoodBasedRecommender        mr = new MoodBasedRecommender();
    private final PersonalityBasedRecommender pr = new PersonalityBasedRecommender();

    @Override
    public List<Song> recommend(User user, SongDatabase db, int limit) {
        List<Song> moodList = mr.recommend(user, db, limit);
        List<Song> persList = pr.recommend(user, db, limit);
        Set<String>  seen   = new HashSet<>();
        List<Song>   merged = new ArrayList<>();
        for (Song s : moodList) if (seen.add(s.getId())) merged.add(s);
        for (Song s : persList) if (seen.add(s.getId())) merged.add(s);
        Collections.shuffle(merged);
        return merged.subList(0, Math.min(merged.size(), limit));
    }
}

// ── Recommender (Context / Facade) ────────────────────────────────────────
/**
 * Recommender selects the appropriate RecommendationStrategy and delegates.
 * Now also supports PlaylistFilter post-processing.
 * Demonstrates: Strategy Pattern, Facade, Open/Closed Principle.
 */
public class Recommender {

    private final SongDatabase db;
    private final Map<String, RecommendationStrategy> strategies;

    public Recommender() {
        this.db         = SongDatabase.getInstance();
        this.strategies = new HashMap<>();
        strategies.put("mood",        new MoodBasedRecommender());
        strategies.put("personality", new PersonalityBasedRecommender());
        strategies.put("hybrid",      new HybridRecommender());
    }

    /** Recommend using a named strategy. */
    public Playlist recommend(User user, String mode, int limit) {
        return recommend(user, mode, limit, PlaylistFilter.all());
    }

    /** Recommend using a named strategy + post-filter. */
    public Playlist recommend(User user, String mode, int limit, PlaylistFilter filter) {
        RecommendationStrategy strat =
            strategies.getOrDefault(mode, new HybridRecommender());
        List<Song> songs = strat.recommend(user, db, limit * 2).stream()
                               .filter(filter::test)
                               .limit(limit)
                               .collect(Collectors.toList());
        String name = cap(user.getCurrentMood()) + " Vibes for " + user.getUsername();
        Playlist pl = new Playlist(name, user.getCurrentMood());
        songs.forEach(pl::addSong);
        return pl;
    }

    /** Pure mood-based playlist (no user needed). */
    public Playlist recommendForMood(String mood, int limit) {
        List<Song> pool = new ArrayList<>(db.getByMood(mood));
        if (pool.isEmpty()) pool = new ArrayList<>(db.getAllSongs());
        Collections.sort(pool);
        Playlist pl = new Playlist("Best for " + cap(mood), mood);
        pool.subList(0, Math.min(pool.size(), limit)).forEach(pl::addSong);
        return pl;
    }

    /** Songs similar to a seed song (same mood or genre). */
    public List<Song> getSimilar(Song seed, int limit) {
        return db.getAllSongs().stream()
            .filter(s -> !s.getId().equals(seed.getId()))
            .filter(s -> s.getMood().equals(seed.getMood())
                      || s.getGenre().equals(seed.getGenre()))
            .sorted().limit(limit).collect(Collectors.toList());
    }

    /** Top-rated songs the user has listened to. */
    public List<Song> topRatedByUser(User user, int limit) {
        Map<String, Double> ratings = user.getAllSongRatings();
        return db.getAllSongs().stream()
            .filter(s -> ratings.containsKey(s.getId()))
            .sorted(Comparator.comparingDouble(s -> -ratings.getOrDefault(s.getId(), s.getRating())))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /** Trending: top N by rating from entire catalogue. */
    public List<Song> trendingToday(int limit) { return db.topRated(limit); }

    private String cap(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}