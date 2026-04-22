package com.riffsphere.modules;

import com.riffsphere.models.Song;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * ApiClient — connects to free, open-source music APIs.
 *
 * APIs used (all free / open):
 * ┌─────────────────────────────────────────────────────────────────────────────────┐
 * │  1. Deezer Public API       https://api.deezer.com/                            │
 * │     – No API key required.  Search, track info, preview URLs (30-sec MP3s).    │
 * │     – Docs: https://developers.deezer.com/api                                  │
 * │                                                                                 │
 * │  2. MusicBrainz API         https://musicbrainz.org/ws/2/                      │
 * │     – 100% open-source metadata. No auth needed.                               │
 * │     – Docs: https://musicbrainz.org/doc/MusicBrainz_API                       │
 * │                                                                                 │
 * │  3. Last.fm API             https://www.last.fm/api                            │
 * │     – Free API key (register at last.fm/api/account/create).                   │
 * │     – Similar tracks, artist bio, top tracks by mood tag.                      │
 * │     – Docs: https://www.last.fm/api/intro                                      │
 * │                                                                                 │
 * │  4. AudioDB API             https://www.theaudiodb.com/api_guide.php           │
 * │     – Artist bio, album art, music video links.                                 │
 * │     – Free tier: test API key = "2"  (limited)                                 │
 * │                                                                                 │
 * │  5. Jamendo API             https://developer.jamendo.com/v3.0                 │
 * │     – 100% Creative Commons music, free streaming.                              │
 * │     – Free client ID required.                                                  │
 * └─────────────────────────────────────────────────────────────────────────────────┘
 *
 * Demonstrates: HTTP integration, JSON parsing (manual — no external libs),
 *               Open/Closed Principle (add new APIs by adding methods).
 */
public class ApiClient {

    private static final String DEEZER_BASE     = "https://api.deezer.com";
    private static final String MUSICBRAINZ_BASE = "https://musicbrainz.org/ws/2";
    private static final String AUDIODB_BASE    = "https://www.theaudiodb.com/api/v1/json/2";
    private static final int    TIMEOUT_MS      = 5000;

    // ── Singleton ────────────────────────────────────────────────
    private static ApiClient instance;
    private ApiClient() {}
    public static synchronized ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    // ══════════════════════════════════════════════════════════════
    //  DEEZER — no API key needed
    // ══════════════════════════════════════════════════════════════

    /**
     * Search Deezer for tracks matching a query.
     * Returns a list of Song objects built from the Deezer response.
     *
     * Endpoint: GET https://api.deezer.com/search?q={query}&limit={limit}
     */
    public List<Song> searchDeezer(String query, int limit) {
        List<Song> results = new ArrayList<>();
        if (query == null || query.isBlank()) return results;
        try {
            String url = DEEZER_BASE + "/search?q="
                       + URLEncoder.encode(query, StandardCharsets.UTF_8)
                       + "&limit=" + Math.min(limit, 50);
            String json = get(url);
            results = parseDeezerTracks(json);
        } catch (Exception e) {
            System.err.println("[ApiClient] Deezer search failed: " + e.getMessage());
        }
        return results;
    }

    /**
     * Fetch top tracks for a mood tag via Deezer genre endpoint.
     * We map moods to Deezer genre IDs.
     *
     * Endpoint: GET https://api.deezer.com/genre/{id}/artists
     */
    public List<Song> getDeezerByGenre(int genreId, int limit) {
        List<Song> results = new ArrayList<>();
        try {
            String url  = DEEZER_BASE + "/chart/" + genreId + "/tracks?limit=" + limit;
            String json = get(url);
            results = parseDeezerTracks(json);
        } catch (Exception e) {
            System.err.println("[ApiClient] Deezer genre fetch failed: " + e.getMessage());
        }
        return results;
    }

    /**
     * Deezer track preview URL builder.
     * Returns the 30-second MP3 preview URL for a given Deezer track ID.
     * Preview URL format: https://cdns-preview-*.dzcdn.net/stream/...
     * (returned in search JSON as "preview" field)
     */
    public String getDeezerPreviewUrl(String trackId) {
        try {
            String url  = DEEZER_BASE + "/track/" + trackId;
            String json = get(url);
            return extractJsonField(json, "preview");
        } catch (Exception e) {
            System.err.println("[ApiClient] Deezer preview fetch failed: " + e.getMessage());
            return null;
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  MUSICBRAINZ — open, no auth
    // ══════════════════════════════════════════════════════════════

    /**
     * Search MusicBrainz recordings (tracks).
     * Returns raw JSON for further parsing.
     *
     * Endpoint: GET https://musicbrainz.org/ws/2/recording/?query={q}&fmt=json&limit={n}
     */
    public String searchMusicBrainz(String query, int limit) {
        try {
            String url = MUSICBRAINZ_BASE + "/recording/?query="
                       + URLEncoder.encode(query, StandardCharsets.UTF_8)
                       + "&fmt=json&limit=" + Math.min(limit, 25);
            // MusicBrainz requires a User-Agent
            return get(url, "RiffSphere/1.0 (https://github.com/riffsphere)");
        } catch (Exception e) {
            System.err.println("[ApiClient] MusicBrainz search failed: " + e.getMessage());
            return "{}";
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  AUDIODB — free test key = "2"
    // ══════════════════════════════════════════════════════════════

    /**
     * Fetch artist info from AudioDB.
     *
     * Endpoint: GET https://www.theaudiodb.com/api/v1/json/2/search.php?s={artistName}
     * Replace "2" with your real free API key from https://www.theaudiodb.com/api_guide.php
     */
    public String getArtistInfo(String artistName) {
        try {
            String url = AUDIODB_BASE + "/search.php?s="
                       + URLEncoder.encode(artistName, StandardCharsets.UTF_8);
            return get(url);
        } catch (Exception e) {
            System.err.println("[ApiClient] AudioDB failed: " + e.getMessage());
            return "{}";
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  LAST.FM — requires free API key
    // ══════════════════════════════════════════════════════════════
    // Register at: https://www.last.fm/api/account/create
    // Then set your key here:
    private static final String LASTFM_KEY  = "YOUR_LASTFM_API_KEY_HERE";
    private static final String LASTFM_BASE = "https://ws.audioscrobbler.com/2.0/";

    /**
     * Search Last.fm for similar tracks to a given song.
     *
     * Endpoint: GET https://ws.audioscrobbler.com/2.0/?method=track.getsimilar
     *              &artist={artist}&track={title}&api_key={key}&format=json&limit={n}
     */
    public String getSimilarTracks(String artist, String title, int limit) {
        if (LASTFM_KEY.startsWith("YOUR")) return "{\"error\":\"No API key set.\"}";
        try {
            String url = LASTFM_BASE
                + "?method=track.getsimilar&artist=" + URLEncoder.encode(artist, "UTF-8")
                + "&track="  + URLEncoder.encode(title, "UTF-8")
                + "&api_key=" + LASTFM_KEY
                + "&format=json&limit=" + limit;
            return get(url);
        } catch (Exception e) {
            System.err.println("[ApiClient] Last.fm failed: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * Fetch top tracks for a mood/tag from Last.fm.
     * Tags: happy, sad, chill, energetic, focus, angry
     *
     * Endpoint: GET https://ws.audioscrobbler.com/2.0/?method=tag.gettoptracks
     *              &tag={mood}&api_key={key}&format=json&limit={n}
     */
    public String getTopTracksByMood(String mood, int limit) {
        if (LASTFM_KEY.startsWith("YOUR")) return "{\"error\":\"No API key set.\"}";
        try {
            String url = LASTFM_BASE
                + "?method=tag.gettoptracks&tag=" + URLEncoder.encode(mood, "UTF-8")
                + "&api_key=" + LASTFM_KEY
                + "&format=json&limit=" + limit;
            return get(url);
        } catch (Exception e) {
            System.err.println("[ApiClient] Last.fm tag failed: " + e.getMessage());
            return "{}";
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Convenience: Mood → Deezer genre ID mapping
    // ══════════════════════════════════════════════════════════════
    public static final Map<String, Integer> MOOD_TO_DEEZER_GENRE = Map.of(
        "happy",     132,   // Pop
        "sad",       152,   // Soul & R&B
        "angry",     197,   // Rock
        "relaxed",   129,   // Jazz
        "energetic",  113,  // Dance
        "focus",      98    // Classical
    );

    // ══════════════════════════════════════════════════════════════
    //  HTTP helpers
    // ══════════════════════════════════════════════════════════════
    private String get(String urlStr) throws IOException {
        return get(urlStr, "RiffSphere-Java/1.0");
    }

    @SuppressWarnings("deprecation")
    private String get(String urlStr, String userAgent) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", userAgent);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        int code = conn.getResponseCode();
        if (code != 200) throw new IOException("HTTP " + code + " for " + urlStr);

        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }

    // ══════════════════════════════════════════════════════════════
    //  Minimal JSON parsing (no external libs required)
    // ══════════════════════════════════════════════════════════════

    /** Extract a simple string field from flat JSON: {"key":"value"} */
    String extractJsonField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start < 0) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return end > start ? json.substring(start, end) : "";
    }

    /** Extract an integer field from JSON. */
    int extractJsonInt(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start < 0) return 0;
        start += search.length();
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end)=='-')) end++;
        try { return Integer.parseInt(json.substring(start, end).trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    /**
     * Parse Deezer JSON response into Song objects.
     * Deezer response format (abbreviated):
     * {
     *   "data": [
     *     {"id":123, "title":"Song", "duration":210, "preview":"https://...",
     *      "artist":{"name":"Artist"}, "album":{"title":"Album"},
     *      "bpm":128, "rank":900000}
     *   ]
     * }
     */
    private List<Song> parseDeezerTracks(String json) {
        List<Song> songs = new ArrayList<>();
        // Split on track objects (everything between { and } inside "data" array)
        int dataStart = json.indexOf("\"data\":[");
        if (dataStart < 0) return songs;

        // Simple state-machine split on top-level objects
        int depth = 0; int objStart = -1;
        boolean inData = false;
        for (int i = dataStart + 8; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth == 0) objStart = i; depth++; }
            else if (c == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    String obj = json.substring(objStart, i + 1);
                    Song s = parseDeezerTrack(obj);
                    if (s != null) songs.add(s);
                    objStart = -1;
                }
            } else if (c == ']' && depth == 0) break;
        }
        return songs;
    }

    private Song parseDeezerTrack(String obj) {
        try {
            String title  = extractJsonField(obj, "title");
            String artist = extractJsonField(obj, "name");   // inside "artist" object
            int    bpm    = extractJsonInt(obj, "bpm");
            if (bpm == 0) bpm = 120; // Deezer doesn't always return BPM
            long   rank   = extractJsonInt(obj, "rank");
            double rating = Math.min(5.0, 1 + rank / 250000.0);

            // Deezer ID as song ID
            String id = "DZ-" + extractJsonInt(obj, "id");
            String preview = extractJsonField(obj, "preview");

            if (title.isBlank()) return null;
            return new Song(id, title, artist.isBlank() ? "Unknown" : artist,
                            "Pop", "relaxed", "chill", bpm, rating, preview);
        } catch (Exception e) {
            return null;
        }
    }
}
