package com.riffsphere.gui;

import com.riffsphere.models.*;
import java.util.*;

/**
 * Command Pattern for reversible UI operations.
 * Demonstrates: Command Pattern, Encapsulation, OCP.
 */
public interface AppCommand {
    void execute();
    void undo();
    String getDescription();
}

// ── Command History (Undo / Redo stack) ──────────────────────────────────
class CommandHistory {
    private final Deque<AppCommand> undoStack = new ArrayDeque<>();
    private final Deque<AppCommand> redoStack = new ArrayDeque<>();
    private static final int MAX = 20;

    public void push(AppCommand cmd) {
        cmd.execute();
        undoStack.push(cmd);
        redoStack.clear();
        if (undoStack.size() > MAX) {
            // remove oldest: convert to list, trim, rebuild
            List<AppCommand> list = new ArrayList<>(undoStack);
            undoStack.clear();
            list.subList(0, MAX).forEach(undoStack::push);
        }
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    public String undo() {
        if (!canUndo()) return "Nothing to undo.";
        AppCommand cmd = undoStack.pop();
        cmd.undo();
        redoStack.push(cmd);
        return "Undone: " + cmd.getDescription();
    }

    public String redo() {
        if (!canRedo()) return "Nothing to redo.";
        AppCommand cmd = redoStack.pop();
        cmd.execute();
        undoStack.push(cmd);
        return "Redone: " + cmd.getDescription();
    }

    public void clear() { undoStack.clear(); redoStack.clear(); }
}

// ── Concrete Commands ─────────────────────────────────────────────────────
class AddFavoriteCommand implements AppCommand {
    private final User user;
    private final Song song;
    AddFavoriteCommand(User user, Song song) { this.user = user; this.song = song; }
    @Override public void execute()          { user.addToFavorites(song); }
    @Override public void undo()             { user.removeFavorite(song); }
    @Override public String getDescription() { return "Add \"" + song.getTitle() + "\" to favorites"; }
}

class RemoveFavoriteCommand implements AppCommand {
    private final User user;
    private final Song song;
    RemoveFavoriteCommand(User user, Song song) { this.user = user; this.song = song; }
    @Override public void execute()             { user.removeFavorite(song); }
    @Override public void undo()                { user.addToFavorites(song); }
    @Override public String getDescription()    { return "Remove \"" + song.getTitle() + "\" from favorites"; }
}

class AddToPlaylistCommand implements AppCommand {
    private final com.riffsphere.modules.PlaylistManager pm;
    private final String playlistName;
    private final Song   song;
    AddToPlaylistCommand(com.riffsphere.modules.PlaylistManager pm,
                         String playlistName, Song song) {
        this.pm = pm; this.playlistName = playlistName; this.song = song;
    }
    @Override public void execute()          { pm.addSong(playlistName, song); }
    @Override public void undo()             { pm.removeSong(playlistName, song); }
    @Override public String getDescription() { return "Add \"" + song.getTitle() + "\" to " + playlistName; }
}
