package com.riffsphere.modules;

import com.riffsphere.models.Song;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Singleton song database.
 * Added: advanced search via PlaylistFilter, shutdown-hook CSV persistence,
 *        Result<T> return types, searchAdvanced().
 * Demonstrates: Singleton, Generic Result<T>, Strategy (PlaylistFilter).
 */
public class SongDatabase {

    private static SongDatabase instance;

    private final List<Song>              allSongs;
    private final Map<String, List<Song>> byMood;
    private final Map<String, List<Song>> byGenre;
    private final Map<String, List<Song>> byPersonality;

    private SongDatabase() {
        allSongs      = new ArrayList<>();
        byMood        = new HashMap<>();
        byGenre       = new HashMap<>();
        byPersonality = new HashMap<>();
        fetchFromJamendo();
    }

    private void fetchFromJamendo() {
        // Try iTunes as it is extremely reliable and has a massive catalog
        boolean itunesLoaded = fetchFromITunes();
        
        // If iTunes fails, try Deezer
        if (!itunesLoaded) fetchFromDeezer();
        
        // Load fallback if we still have very few songs
        if (allSongs.size() < 10) loadFallbackSongs();
    }

    private boolean fetchFromITunes() {
        String[] genres = {"Pop", "Rock", "Jazz", "Electronic", "Classical", "Hip Hop", "Lofi"};
        boolean success = false;
        
        for (String genre : genres) {
            String url = "https://itunes.apple.com/search?term=" + genre.replace(" ", "+") + "&entity=song&limit=40";
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode results = root.get("results");
                
                if (results != null && results.isArray()) {
                    for (JsonNode node : results) {
                        String id = "IT-" + (node.has("trackId") ? node.get("trackId").asText() : UUID.randomUUID().toString());
                        String title = node.has("trackName") ? node.get("trackName").asText() : "Unknown";
                        String artist = node.has("artistName") ? node.get("artistName").asText() : "Unknown";
                        String audio = node.has("previewUrl") ? node.get("previewUrl").asText() : "";
                        String g = node.has("primaryGenreName") ? node.get("primaryGenreName").asText() : genre;
                        
                        if (audio.isEmpty()) continue;

                        // Mood & Personality mapping
                        String mood = "relaxed";
                        String pers = "vibe";
                        
                        String lowerGenre = g.toLowerCase();
                        if (lowerGenre.contains("pop") || lowerGenre.contains("dance")) {
                            mood = "happy"; pers = "vibe";
                        } else if (lowerGenre.contains("rock") || lowerGenre.contains("metal")) {
                            mood = "energetic"; pers = "energizer";
                        } else if (lowerGenre.contains("jazz") || lowerGenre.contains("soul")) {
                            mood = "relaxed"; pers = "dreamer";
                        } else if (lowerGenre.contains("classical") || lowerGenre.contains("ambient")) {
                            mood = "focus"; pers = "dreamer";
                        } else if (lowerGenre.contains("hip hop") || lowerGenre.contains("rap") || lowerGenre.contains("electronic")) {
                            mood = "energetic"; pers = "beat";
                        } else if (lowerGenre.contains("indie") || lowerGenre.contains("acoustic") || lowerGenre.contains("folk")) {
                            mood = "sad"; pers = "soul";
                        }

                        addSong(new Song.Builder()
                            .id(id).title(title).artist(artist).genre(g)
                            .mood(mood).personality(pers).bpm(120)
                            .audioUrl(audio).rating(3.0 + Math.random() * 2)
                            .build());
                    }
                    success = true;
                }
            } catch (Exception e) {
                System.err.println("--- SONG DATABASE: iTunes genre " + genre + " failed ---");
            }
        }
        if (success) System.out.println("--- SONG DATABASE: Successfully loaded " + allSongs.size() + " tracks from iTunes ---");
        return success;
    }

    private boolean fetchFromDeezer() {
        String url = "https://api.deezer.com/search?q=vibe&limit=100";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode results = root.get("data");
            
            if (results != null && results.isArray() && results.size() > 0) {
                for (JsonNode node : results) {
                    String id = "DZ-" + node.get("id").asText();
                    String title = node.get("title").asText();
                    String artist = node.get("artist").get("name").asText();
                    String audio = node.get("preview").asText();
                    
                    // Meta-mapping: Assign personality/mood based on track rank/random for diversity
                    int rank = node.get("rank").asInt();
                    String mood = (rank % 2 == 0) ? "energetic" : "relaxed";
                    String personality = (rank % 3 == 0) ? "energizer" : (rank % 3 == 1 ? "dreamer" : "vibe");
                    
                    addSong(new Song.Builder()
                        .id(id).title(title).artist(artist).genre("Pop")
                        .mood(mood).personality(personality).bpm(120)
                        .audioUrl(audio).rating(4.0 + Math.random())
                        .build());
                }
                System.out.println("--- SONG DATABASE: Successfully loaded " + allSongs.size() + " tracks from Deezer ---");
                return true;
            }
        } catch (Exception e) {
            System.err.println("--- SONG DATABASE: Deezer API failed, trying fallback... ---");
        }
        return false;
    }

    private void loadFallbackSongs() {
        String[] data = {
            "S001,Happy,Pharrell Williams,Pop,happy,party,160,4.8,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
            "S002,Can't Stop the Feeling,Justin Timberlake,Pop,happy,party,113,4.7,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
            "S003,Rolling in the Deep,Adele,Soul,sad,soul,105,4.9,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
            "S004,Shape of You,Ed Sheeran,Pop,happy,vibe,96,4.6,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
            "S005,Blinding Lights,The Weeknd,Synthpop,energetic,energizer,171,4.7,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
            "S006,Someone You Loved,Lewis Capaldi,Pop,sad,soul,110,4.8,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
            "S007,Dance Monkey,Tones and I,Pop,happy,vibe,98,4.5,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
            "S008,Stay,The Kid LAROI,Pop,energetic,energizer,170,4.6,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
            "S009,Levitating,Dua Lipa,Pop,happy,vibe,103,4.7,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
            "S010,Bad Guy,Billie Eilish,Pop,chill,chill,135,4.9,https://www.soundhelix.com/examples/mp3/SoundHelix-Song-13.mp3"
        };
        for (String line : data) {
            Song s = Song.fromCsv(line);
            if (s != null) addSong(s);
        }
        System.out.println("--- SONG DATABASE: Fallback loaded " + allSongs.size() + " local tracks ---");
    }

    public static synchronized SongDatabase getInstance() {
        if (instance == null) instance = new SongDatabase();
        return instance;
    }



    // ── Mutation ──────────────────────────────────────────────────
    public void addSong(Song song) {
        if (song == null || allSongs.contains(song)) return;
        allSongs.add(song);
        byMood.computeIfAbsent(song.getMood(),        k -> new ArrayList<>()).add(song);
        byGenre.computeIfAbsent(song.getGenre(),      k -> new ArrayList<>()).add(song);
        byPersonality.computeIfAbsent(song.getPersonality(), k -> new ArrayList<>()).add(song);
    }

    // ── Queries ───────────────────────────────────────────────────
    public List<Song> getAllSongs()              { return Collections.unmodifiableList(allSongs); }
    public List<Song> getByMood(String m)        { return byMood.getOrDefault(m.toLowerCase(),            new ArrayList<>()); }
    public List<Song> getByGenre(String g)       { return byGenre.getOrDefault(g,                         new ArrayList<>()); }
    public List<Song> getByPersonality(String p) { return byPersonality.getOrDefault(p.toLowerCase(),     new ArrayList<>()); }
    public Optional<Song> findSong(String id)    { return allSongs.stream().filter(s -> s.getId().equals(id)).findFirst(); }
    public int        total()                    { return allSongs.size(); }
    public Set<String> allMoods()               { return new TreeSet<>(byMood.keySet()); }
    public Set<String> allGenres()              { return new TreeSet<>(byGenre.keySet()); }

    /** Simple keyword search (title / artist / genre). */
    public List<Song> search(String query) {
        String q = query.toLowerCase();
        return allSongs.stream()
            .filter(s -> s.getTitle().toLowerCase().contains(q)
                      || s.getArtist().toLowerCase().contains(q)
                      || s.getGenre().toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    /** Advanced filter-based search (PlaylistFilter Strategy). */
    public List<Song> searchAdvanced(PlaylistFilter filter) {
        if (filter == null) return getAllSongs().stream().collect(Collectors.toList());
        return allSongs.stream().filter(filter::test).collect(Collectors.toList());
    }

    /** Top N songs by rating across entire catalogue. */
    public List<Song> topRated(int n) {
        return allSongs.stream().sorted().limit(n).collect(Collectors.toList());
    }

    // ── CSV persistence ──────────────────────────────────────────
    public void saveToCsv(String filePath) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filePath))) {
            w.write("id,title,artist,genre,mood,personality,bpm,rating"); w.newLine();
            for (Song s : allSongs) { w.write(s.toCsv()); w.newLine(); }
        }
    }

    public void loadFromCsv(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) throw new FileNotFoundException("Not found: " + filePath);
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String line; boolean first = true;
            while ((line = r.readLine()) != null) {
                if (first) { first = false; continue; }
                Song s = Song.fromCsv(line);
                if (s != null) addSong(s);
            }
        }
    }
}