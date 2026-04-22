package com.riffsphere.models;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Playlist implements Iterable<Song> (Iterator Pattern) and carries a PlaylistType enum.
 * Demonstrates: Iterator Pattern, Enum, Composition, Encapsulation.
 */
public class Playlist implements Iterable<Song> {

    // ── Enum for playlist classification ──────────────────────────
    public enum PlaylistType { AUTO_GENERATED, USER_CREATED, SYSTEM }

    private String        name;
    private String        description;
    private String        mood;
    private String        personalityTag;
    private final List<Song>    songs;
    private String        createdBy;
    private PlaylistType  type;
    private final LocalDateTime createdAt;

    public Playlist(String name, String description,
                    String mood, String personalityTag, String createdBy) {
        this.name           = name;
        this.description    = description;
        this.mood           = mood;
        this.personalityTag = personalityTag;
        this.createdBy      = createdBy;
        this.songs          = new ArrayList<>();
        this.type           = PlaylistType.USER_CREATED;
        this.createdAt      = LocalDateTime.now();
    }

    public Playlist(String name, String mood) {
        this(name, "Auto-generated", mood, "all", "system");
        this.type = PlaylistType.AUTO_GENERATED;
    }

    // ── Getters ──────────────────────────────────────────────────
    public String        getName()           { return name; }
    public String        getDescription()    { return description; }
    public String        getMood()           { return mood; }
    public String        getPersonalityTag() { return personalityTag; }
    public String        getCreatedBy()      { return createdBy; }
    public PlaylistType  getType()           { return type; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public int           getSongCount()      { return songs.size(); }
    public List<Song>    getSongs()          { return Collections.unmodifiableList(songs); }

    // ── Mutators ─────────────────────────────────────────────────
    public void setType(PlaylistType t)        { this.type        = t; }
    public void setDescription(String d)       { this.description = d; }
    public void setName(String n)              { if (n != null && !n.isBlank()) this.name = n; }

    public void addSong(Song song) {
        if (song != null && !songs.contains(song)) songs.add(song);
    }
    public void addAll(Collection<Song> s) { if (s != null) s.forEach(this::addSong); }
    public void removeSong(Song song)      { songs.remove(song); }
    public void sortByRating()             { Collections.sort(songs); }
    public void shuffle()                  { Collections.shuffle(songs); }
    public void clear()                    { songs.clear(); }

    // ── Iterable implementation (Iterator Pattern) ────────────────
    @Override
    public Iterator<Song> iterator() { return songs.iterator(); }

    // ── Export ───────────────────────────────────────────────────
    public void toFile(Path path) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path.toFile()))) {
            w.write("# Riffsphere Playlist Export");       w.newLine();
            w.write("# Name: "      + name);               w.newLine();
            w.write("# Mood: "      + mood);               w.newLine();
            w.write("# Created: "   + createdAt.format(fmt)); w.newLine();
            w.write("# Songs: "     + songs.size());       w.newLine();
            w.write("id,title,artist,genre,mood,personality,bpm,rating"); w.newLine();
            for (Song s : songs) { w.write(s.toCsv()); w.newLine(); }
        }
    }

    @Override
    public String toString() {
        return String.format("Playlist[\"%s\" | mood:%s | %d songs | by:%s | %s]",
            name, mood, songs.size(), createdBy, type);
    }
}