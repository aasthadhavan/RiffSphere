package com.riffsphere.modules;

import com.riffsphere.models.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton CRUD manager for user-created playlists.
 * Fires AppEvents through EventBus on every mutation.
 * Demonstrates: Singleton, Observer (via EventBus), CRUD, Encapsulation.
 */
public class PlaylistManager {

    private static PlaylistManager instance;
    private final Map<String, Playlist> playlists = new LinkedHashMap<>();
    private final EventBus              bus        = EventBus.getInstance();

    private PlaylistManager() {}

    public static synchronized PlaylistManager getInstance() {
        if (instance == null) instance = new PlaylistManager();
        return instance;
    }

    // ── Create ───────────────────────────────────────────────────
    public Result<Playlist> create(String name, String mood, String createdBy) {
        if (name == null || name.isBlank())
            return Result.fail("Playlist name cannot be empty.");
        if (playlists.containsKey(name))
            return Result.fail("A playlist named \"" + name + "\" already exists.");
        Playlist pl = new Playlist(name, "My playlist", mood, "all", createdBy);
        pl.setType(Playlist.PlaylistType.USER_CREATED);
        playlists.put(name, pl);
        bus.publish(AppEvent.PLAYLIST_CREATED);
        return Result.ok(pl, "Playlist \"" + name + "\" created.");
    }

    // ── Read ─────────────────────────────────────────────────────
    public Optional<Playlist> get(String name)       { return Optional.ofNullable(playlists.get(name)); }
    public Collection<Playlist> getAll()             { return Collections.unmodifiableCollection(playlists.values()); }
    public boolean exists(String name)               { return playlists.containsKey(name); }

    // ── Update ───────────────────────────────────────────────────
    public Result<Void> addSong(String playlistName, Song song) {
        Playlist pl = playlists.get(playlistName);
        if (pl == null) return Result.fail("Playlist not found: " + playlistName);
        if (song == null) return Result.fail("Song cannot be null.");
        pl.addSong(song);
        bus.publish(AppEvent.PLAYLIST_UPDATED);
        return Result.ok(null, "Song added.");
    }

    public Result<Void> removeSong(String playlistName, Song song) {
        Playlist pl = playlists.get(playlistName);
        if (pl == null) return Result.fail("Playlist not found: " + playlistName);
        pl.removeSong(song);
        bus.publish(AppEvent.PLAYLIST_UPDATED);
        return Result.ok(null, "Song removed.");
    }

    public Result<Void> rename(String oldName, String newName) {
        if (!playlists.containsKey(oldName)) return Result.fail("Playlist not found.");
        if (playlists.containsKey(newName))  return Result.fail("Name already taken.");
        Playlist pl = playlists.remove(oldName);
        pl.setName(newName);
        playlists.put(newName, pl);
        bus.publish(AppEvent.PLAYLIST_UPDATED);
        return Result.ok(null, "Renamed.");
    }

    // ── Delete ───────────────────────────────────────────────────
    public Result<Void> delete(String name) {
        if (!playlists.containsKey(name)) return Result.fail("Playlist not found: " + name);
        playlists.remove(name);
        bus.publish(AppEvent.PLAYLIST_DELETED);
        return Result.ok(null, "Playlist deleted.");
    }

    /** Clear all managed playlists (on logout). */
    public void reset() { playlists.clear(); }
}
