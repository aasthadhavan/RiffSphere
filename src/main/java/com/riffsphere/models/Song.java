package com.riffsphere.models;

/**
 * Song extends MediaItem (Inheritance) and implements Comparable (Polymorphism).
 * Uses a static inner Builder class (Builder Pattern).
 * Demonstrates: Inheritance, Encapsulation, Builder Pattern, Comparable.
 */
public class Song extends MediaItem implements Comparable<Song> {

    private String artist;
    private String genre;
    private String mood;
    private String personality;
    private int    bpm;
    private double rating;
    private int    playCount;
    private String audioUrl;

    // ── Builder-based private constructor ──────────────────────────
    private Song(Builder b) {
        super(b.id, b.title);
        this.artist      = b.artist;
        this.genre       = b.genre;
        this.mood        = b.mood.toLowerCase();
        this.personality = b.personality.toLowerCase();
        this.bpm         = b.bpm;
        this.rating      = b.rating;
        this.audioUrl    = b.audioUrl;
        this.playCount   = 0;
    }

    // ── Legacy full constructor (for fromCsv, SongDatabase) ────────
    public Song(String id, String title, String artist, String genre,
                String mood, String personality, int bpm, double rating, String audioUrl) {
        super(id, title);
        this.artist      = artist;
        this.genre       = genre;
        this.mood        = mood.toLowerCase();
        this.personality = personality.toLowerCase();
        this.bpm         = bpm;
        this.rating      = rating;
        this.audioUrl    = audioUrl;
        this.playCount   = 0;
    }

    public Song(String title, String artist, String genre, String mood) {
        this("USR-" + System.currentTimeMillis(),
             title, artist, genre, mood, "chill", 120, 3.5, "");
    }

    // ── Getters ───────────────────────────────────────────────────
    public String getArtist()      { return artist; }
    public String getGenre()       { return genre; }
    public String getMood()        { return mood; }
    public String getPersonality() { return personality; }
    public int    getBpm()         { return bpm; }
    public double getRating()      { return rating; }
    public String getAudioUrl()    { return audioUrl; }
    public int    getPlayCount()   { return playCount; }

    // ── Mutators ──────────────────────────────────────────────────
    public void setRating(double r)  { if (r >= 0.0 && r <= 5.0) this.rating = r; }
    public void incrementPlayCount() { playCount++; }

    // ── MediaItem abstract implementations ──────────────────────────
    @Override
    public String getDisplayInfo() {
        return String.format("\"%s\" by %s [%s/%s] %d BPM %.1f★",
            title, artist, genre, mood, bpm, rating);
    }

    @Override
    public String toCsv() {
        return String.join(",", id, title, artist, genre, mood, personality,
            String.valueOf(bpm), String.valueOf(rating));
    }

    // ── Comparable: sort by rating descending ──────────────────────
    @Override
    public int compareTo(Song other) {
        return Double.compare(other.rating, this.rating);
    }

    public static Song fromCsv(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 8) return null;
        try {
            return new Song(
                p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                p[4].trim(), p[5].trim(),
                Integer.parseInt(p[6].trim()),
                Double.parseDouble(p[7].trim()),
                p.length > 8 ? p[8].trim() : "");
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() { return getDisplayInfo(); }

    // ── Static Builder inner class ─────────────────────────────────
    public static class Builder {
        private String id          = "USR-" + System.currentTimeMillis();
        private String title       = "Untitled";
        private String artist      = "Unknown";
        private String genre       = "Pop";
        private String mood        = "relaxed";
        private String personality = "chill";
        private int    bpm         = 120;
        private double rating      = 3.5;
        private String audioUrl    = "";

        public Builder id(String id)           { this.id = id;               return this; }
        public Builder title(String title)     { this.title = title;         return this; }
        public Builder artist(String artist)   { this.artist = artist;       return this; }
        public Builder genre(String genre)     { this.genre = genre;         return this; }
        public Builder mood(String mood)       { this.mood = mood;           return this; }
        public Builder personality(String p)   { this.personality = p;       return this; }
        public Builder bpm(int bpm)            { this.bpm = bpm;             return this; }
        public Builder rating(double rating)   { this.rating = rating;       return this; }
        public Builder audioUrl(String url)    { this.audioUrl = url;        return this; }
        public Song    build()                 { return new Song(this); }
    }
}