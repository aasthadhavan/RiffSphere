package com.riffsphere.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Enhanced User model.
 * Demonstrates: Encapsulation, Composition (UserStats, Playlist, Rating),
 *               Password hashing (SHA-256), Association.
 */
public class User {

    private String     username;
    private String     passwordHash;   // SHA-256 hash — never stored as plain text
    private String     email;
    private String     personalityType;
    private String     currentMood;
    private final List<Song>              favorites;
    private final List<Song>              history;
    private int[]                         personalityScores;
    private final Map<String, Double>     songRatings;      // songId → user's rating
    private final Map<String, Playlist>   customPlaylists;  // name → Playlist
    private final UserStats               stats;

    public User(String username, String password, String email) {
        this.username          = username;
        this.passwordHash      = hash(password);
        this.email             = email != null ? email : "";
        this.personalityType   = "unknown";
        this.currentMood       = "relaxed";
        this.favorites         = new ArrayList<>();
        this.history           = new ArrayList<>();
        this.personalityScores = new int[]{0, 0, 0, 0};
        this.songRatings       = new LinkedHashMap<>();
        this.customPlaylists   = new LinkedHashMap<>();
        this.stats             = new UserStats();
    }

    // ── Password ─────────────────────────────────────────────────
    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return raw; // fallback: plain text (should never happen on any JVM)
        }
    }

    public boolean checkPassword(String input) {
        // Support both hashed and legacy plain-text checks
        return passwordHash.equals(hash(input)) || passwordHash.equals(input);
    }

    // ── Core getters ─────────────────────────────────────────────
    public String   getUsername()          { return username; }
    public String   getEmail()             { return email; }
    public String   getPersonalityType()   { return personalityType; }
    public String   getCurrentMood()       { return currentMood; }
    public int[]    getPersonalityScores() { return personalityScores.clone(); }
    public UserStats getStats()            { return stats; }

    // ── Core setters ─────────────────────────────────────────────
    public void setCurrentMood(String m) {
        this.currentMood = m.toLowerCase();
        stats.recordMood(m);
    }
    public void setPersonalityType(String t)  { this.personalityType = t; }
    public void setPersonalityScores(int[] s) { this.personalityScores = s.clone(); }

    // ── Favorites ────────────────────────────────────────────────
    public List<Song> getFavorites()         { return new ArrayList<>(favorites); }
    public void addToFavorites(Song song)    { if (song != null && !favorites.contains(song)) favorites.add(song); }
    public void removeFavorite(Song song)    { favorites.remove(song); }
    public boolean isFavorite(Song song)     { return favorites.contains(song); }

    // ── History ──────────────────────────────────────────────────
    public List<Song> getHistory() { return new ArrayList<>(history); }
    public void addToHistory(Song song) {
        if (song == null) return;
        history.remove(song);
        history.add(0, song);
        if (history.size() > 50) history.remove(history.size() - 1);
        stats.recordPlay(song);
    }

    // ── Per-user Song Ratings ────────────────────────────────────
    public void rateSong(Song song, double rating) {
        if (song == null || rating < 0 || rating > 5) return;
        songRatings.put(song.getId(), rating);
        stats.updateAvgRating(songRatings);
    }
    public double getSongRating(Song song) {
        return songRatings.getOrDefault(song.getId(), song.getRating());
    }
    public Map<String, Double> getAllSongRatings() { return Collections.unmodifiableMap(songRatings); }

    // ── Custom Playlists ─────────────────────────────────────────
    public void addCustomPlaylist(Playlist pl)         { if (pl != null) customPlaylists.put(pl.getName(), pl); }
    public void removeCustomPlaylist(String name)      { customPlaylists.remove(name); }
    public Playlist getCustomPlaylist(String name)     { return customPlaylists.get(name); }
    public Map<String, Playlist> getCustomPlaylists()  { return Collections.unmodifiableMap(customPlaylists); }

    @Override
    public String toString() {
        return String.format("User{%s | mood:%s | type:%s | favs:%d | plays:%d}",
            username, currentMood, personalityType, favorites.size(), stats.getTotalPlays());
    }
}