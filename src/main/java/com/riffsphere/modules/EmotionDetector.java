package com.riffsphere.modules;

import java.util.*;

interface MoodAnalyzer {
    String detectMood(String input);
    Map<String, Integer> getMoodScores(String input);
}

abstract class BaseMoodAnalyzer implements MoodAnalyzer {

    protected Map<String, List<String>> moodKeywords;

    protected void initKeywords() {
        moodKeywords = new HashMap<>();
        moodKeywords.put("happy", Arrays.asList(
            "happy","joy","excited","great","wonderful","amazing",
            "fantastic","love","smile","fun","awesome","cheerful","glad","elated"));
        moodKeywords.put("sad", Arrays.asList(
            "sad","cry","depressed","lonely","heartbroken","miss",
            "lost","grief","hopeless","tears","down","blue","melancholy","alone"));
        moodKeywords.put("angry", Arrays.asList(
            "angry","mad","furious","rage","hate","frustrated",
            "annoyed","irritated","stressed","livid","outraged","upset"));
        moodKeywords.put("relaxed", Arrays.asList(
            "relaxed","calm","peaceful","chill","comfortable","serene",
            "tranquil","easy","mellow","content","lazy","breezy","quiet"));
        moodKeywords.put("energetic", Arrays.asList(
            "energetic","pumped","motivated","workout","gym","run",
            "dance","hype","active","power","ready","strong"));
        moodKeywords.put("focus", Arrays.asList(
            "focus","study","work","concentrate","productive","think",
            "learn","exam","deadline","code","write","read","homework","project"));
    }

    @Override
    public Map<String, Integer> getMoodScores(String input) {
        String lower = input.toLowerCase();
        Map<String, Integer> scores = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : moodKeywords.entrySet()) {
            int score = 0;
            for (String kw : entry.getValue())
                if (lower.contains(kw)) score++;
            scores.put(entry.getKey(), score);
        }
        return scores;
    }
}

class TextMoodAnalyzer extends BaseMoodAnalyzer {

    public TextMoodAnalyzer() { initKeywords(); }

    @Override
    public String detectMood(String text) {
        if (text == null || text.isBlank()) return "relaxed";
        return getMoodScores(text).entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("relaxed");
    }
}

public class EmotionDetector {

    private final MoodAnalyzer analyzer;

    private static final Map<String, String> MESSAGES = new LinkedHashMap<>() {{
        put("happy",     "Great energy! Let's keep it going.");
        put("sad",       "Sending warmth your way. Music heals.");
        put("angry",     "Channel that energy into something powerful.");
        put("relaxed",   "Perfect vibe for a chill session.");
        put("energetic", "Time to move! Let's get pumped.");
        put("focus",     "Focus mode activated. Deep work time.");
    }};

    public EmotionDetector() {
        this.analyzer = new TextMoodAnalyzer();
    }

    public String detectFromText(String text)  { return analyzer.detectMood(text); }

    public String setMoodDirectly(String mood) {
        return MESSAGES.containsKey(mood.toLowerCase()) ? mood.toLowerCase() : "relaxed";
    }

    public String getMoodMessage(String mood) {
        return MESSAGES.getOrDefault(mood.toLowerCase(),
               "Let's find your perfect soundtrack.");
    }

    public Map<String, Integer> getDetailedScores(String text) {
        return analyzer.getMoodScores(text);
    }

    public static String moodLabel(String mood) {
        return switch (mood.toLowerCase()) {
            case "happy"     -> "Happy";
            case "sad"       -> "Sad";
            case "angry"     -> "Angry";
            case "relaxed"   -> "Relaxed";
            case "energetic" -> "Energetic";
            case "focus"     -> "Focus";
            default          -> "Music";
        };
    }
}