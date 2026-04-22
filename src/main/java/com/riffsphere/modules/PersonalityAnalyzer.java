package com.riffsphere.modules;

import com.riffsphere.models.User;
import java.util.*;

/**
 * Improved PersonalityAnalyzer — 15 questions, 5 options each, 8 personality types.
 *
 * Personality score indices:
 *   0 = Chill Listener    1 = Social Energiser
 *   2 = Deep Focus        3 = Emotional Explorer
 *   4 = Rhythm Seeker     5 = Reflective Wanderer
 *   6 = Power Surge       7 = Creative Soul
 *
 * Each question option maps to one of the 8 types.
 */
public class PersonalityAnalyzer {

    // ── Static inner classes ──────────────────────────────────────
    public static class Question {
        private final String   text;
        private final String[] options;
        private final int[]    personalityMap; // index per option → personality type

        public Question(String text, String[] options, int[] personalityMap) {
            this.text           = text;
            this.options        = options;
            this.personalityMap = personalityMap;
        }

        public String   getText()    { return text; }
        public String[] getOptions() { return options.clone(); }

        public int getPersonalityFor(int optIdx) {
            return (optIdx >= 0 && optIdx < personalityMap.length)
                   ? personalityMap[optIdx] : 0;
        }
    }

    public static class PersonalityResult {
        public final String   type;
        public final String   title;
        public final String   description;
        public final String   tagline;
        public final String   emoji;
        public final String[] preferredMoods;
        public final int[]    scores;

        public PersonalityResult(String type, String title, String tagline,
                                 String description, String emoji,
                                 String[] preferredMoods, int[] scores) {
            this.type          = type;
            this.title         = title;
            this.tagline       = tagline;
            this.description   = description;
            this.emoji         = emoji;
            this.preferredMoods = preferredMoods;
            this.scores        = scores;
        }
    }

    // ── 15 Questions × 5 options, each option → personality type ──
    private static final List<Question> QUESTIONS = List.of(

        new Question(
            "It's Friday evening. What's your move?",
            new String[]{
                "A  Headphones in, soft playlist, zero interruptions",
                "B  Group chat lit, where's the party?",
                "C  Noise-cancelling on, deep work session",
                "D  Journaling with a sad playlist on repeat",
                "E  Spontaneous drive with a banger playlist"
            }, new int[]{0, 1, 2, 3, 4}),

        new Question(
            "Which word describes your music taste best?",
            new String[]{
                "A  Atmospheric",
                "B  Energising",
                "C  Focused",
                "D  Meaningful",
                "E  Groovy"
            }, new int[]{0, 1, 2, 3, 4}),

        new Question(
            "You're heartbroken. What do you reach for?",
            new String[]{
                "A  A soft ambient mix — just let me breathe",
                "B  Call friends and blast something fun to distract myself",
                "C  A structured lo-fi playlist to keep my mind busy",
                "D  That one song that perfectly captures the feeling — on loop",
                "E  High-energy music to channel the anger"
            }, new int[]{0, 1, 2, 3, 6}),

        new Question(
            "Your perfect concert experience?",
            new String[]{
                "A  Intimate acoustic show in a candlelit basement venue",
                "B  Massive festival — 60,000 people, lasers, confetti",
                "C  Live film score performance in a concert hall",
                "D  Solo artist pours their soul out on stage",
                "E  Underground club night — bass you can feel in your chest"
            }, new int[]{0, 1, 2, 3, 4}),

        new Question(
            "When you discover a new song you love, you:",
            new String[]{
                "A  Save it quietly to a private playlist",
                "B  Immediately send it to everyone you know",
                "C  Research the artist's influences and discography",
                "D  Listen 10 times, dissecting every lyric",
                "E  Add it to your workout mix right away"
            }, new int[]{5, 1, 2, 3, 4}),

        new Question(
            "Which playlist name is most 'you'?",
            new String[]{
                "A  Sunday Morning Slow Flow",
                "B  Pre-Game Hype Mix",
                "C  4 AM Deep Work Session",
                "D  The Feelings Playlist",
                "E  Sunrise Run — Push It"
            }, new int[]{0, 1, 2, 3, 4}),

        new Question(
            "How do you typically listen to music?",
            new String[]{
                "A  Low volume in the background — ambient texture",
                "B  Loud, I sing along to every word",
                "C  With noise-cancelling headphones, fully immersed",
                "D  I sit still and close my eyes — full attention",
                "E  While moving — walking, running, cooking"
            }, new int[]{0, 1, 2, 3, 4}),

        new Question(
            "Pick your ideal creative space:",
            new String[]{
                "A  Quiet mountain cabin with rain hitting the window",
                "B  Rooftop party overlooking the city",
                "C  Minimalist studio with whiteboards everywhere",
                "D  Dimly lit room with fairy lights and a journal",
                "E  A moving train, window seat, headphones in"
            }, new int[]{0, 1, 2, 3, 5}),

        new Question(
            "What genre could you listen to exclusively for a week?",
            new String[]{
                "A  Ambient / Lo-Fi / Chillwave",
                "B  Pop / Hip-Hop / Dance",
                "C  Classical / Film Scores / Instrumental",
                "D  Indie Folk / Singer-Songwriter / Soul",
                "E  Electronic / House / Drum & Bass"
            }, new int[]{0, 1, 2, 3, 4}),

        new Question(
            "When you're stressed, music does what for you?",
            new String[]{
                "A  Slows my nervous system — instant calm",
                "B  Gets me out of my head into the room",
                "C  Filters distractions so I can problem-solve",
                "D  Validates how I feel, it's therapeutic",
                "E  Converts stress into raw energy"
            }, new int[]{0, 1, 2, 3, 6}),

        new Question(
            "Be honest — how often do you skip songs?",
            new String[]{
                "A  Almost never — I let albums play through",
                "B  Constantly — I need the energy up at all times",
                "C  Rarely — I queue with intention",
                "D  Only when the song doesn't match my mood",
                "E  A lot — I'm chasing the perfect vibe"
            }, new int[]{5, 1, 2, 3, 7}),

        new Question(
            "Your go-to listening time is:",
            new String[]{
                "A  Early morning — sets the peaceful tone",
                "B  Evening out or pre-event — hype mode",
                "C  Mid-day — background while working",
                "D  Late night — when the world goes quiet",
                "E  During physical activity — gym, runs, walks"
            }, new int[]{0, 1, 2, 3, 4}),

        new Question(
            "A song's most important element is:",
            new String[]{
                "A  The texture and atmosphere it creates",
                "B  The beat and how it makes people move",
                "C  The compositional complexity and arrangement",
                "D  The emotional truth in the lyrics",
                "E  The production energy and dynamics"
            }, new int[]{0, 1, 2, 3, 7}),

        new Question(
            "How do you build playlists?",
            new String[]{
                "A  Carefully curated mood boards — hours of work",
                "B  Hit shuffle on anything, vibe decides",
                "C  Themed and titled, with transitions planned",
                "D  It mirrors my emotional state — very personal",
                "E  By function: gym, sleep, study, drive — each separate"
            }, new int[]{0, 1, 7, 3, 2}),

        new Question(
            "Last one — you're writing your own soundtrack. It's:",
            new String[]{
                "A  A slow, expansive ambient piece with no lyrics",
                "B  A festival-opening banger everyone sings along to",
                "C  A layered orchestral piece that rewards every listen",
                "D  A confessional folk song that makes strangers cry",
                "E  A high-tempo electronic track with an unstoppable drop"
            }, new int[]{0, 1, 2, 3, 4})
    );

    // ── 8 Personality Profiles ────────────────────────────────────
    private static final Object[][] PROFILES = {
        // {type, title, tagline, description, emoji, moods}
        {"chill",       "The Chill Listener",
         "Music is your sanctuary.",
         "You treat music as a personal retreat. Ambient textures, soft acoustics, and the perfect late-night playlist are your love language. You're selective, intentional, and deeply private about what you listen to. Your ears lead you home.",
         "🌙", new String[]{"relaxed", "focus"}},

        {"social",      "The Social Energiser",
         "Music is meant to be shared.",
         "For you, music is the life of every room. You curate for crowds, hype playlists are sacred, and you've converted at least five people to new artists this year. High energy is your comfort zone — silence feels wasteful.",
         "🎉", new String[]{"happy", "energetic"}},

        {"focus",       "The Deep Focus",
         "Music is a productivity tool.",
         "You use music with surgical precision. Instrumental lo-fi, film scores, and ambient tracks help you enter deep flow states where time disappears. You're disciplined, intentional, and treat your listening environment like an engineer treats a cockpit.",
         "🔬", new String[]{"focus", "relaxed"}},

        {"emotional",   "The Emotional Explorer",
         "Every lyric is a mirror.",
         "You feel music in your chest. A single well-written verse can wreck you in the best possible way. Your playlists are emotional diaries. You don't just listen — you experience. Music is your most reliable therapist.",
         "🫀", new String[]{"sad", "relaxed"}},

        {"rhythm",      "The Rhythm Seeker",
         "If it doesn't move you, skip.",
         "You live for the groove. BPM matters to you. Whether it's a morning run, the gym, or just dinner prep, you need the beat to carry you. You're spontaneous, present-focused, and your playlists are chronically underrated.",
         "⚡", new String[]{"energetic", "happy"}},

        {"wanderer",    "The Reflective Wanderer",
         "Music makes the journey feel like meaning.",
         "You listen in transit — metaphorically and literally. Long drives, train rides, midnight walks. Your music is cinematic and unhurried. You're a slow reader of lyrics and often discover gems years after release.",
         "🌄", new String[]{"relaxed", "sad"}},

        {"power",       "The Power Surge",
         "Music is fuel. Pure and simple.",
         "You need intensity. Whether you're channelling frustration, pushing through a wall, or just need to feel powerful — music is your weapon. Hard rock, aggressive hip-hop, and high-energy anthems define your library.",
         "🔥", new String[]{"angry", "energetic"}},

        {"creative",    "The Creative Soul",
         "Music is your canvas and your compass.",
         "You approach music the way an artist approaches a blank page. Production details fascinate you. You hear things others miss — the reverb tail, the counter-melody, the breath before the chorus. You probably make music, or secretly want to.",
         "🎨", new String[]{"focus", "relaxed"}}
    };

    public static List<Question> getQuestions() { return QUESTIONS; }

    public PersonalityResult analyze(int[] answers) {
        int[] scores = new int[8];
        for (int i = 0; i < answers.length && i < QUESTIONS.size(); i++) {
            int opt = answers[i];
            if (opt >= 0 && opt < 5)
                scores[QUESTIONS.get(i).getPersonalityFor(opt)]++;
        }
        // Find dominant type
        int maxIdx = 0;
        for (int i = 1; i < scores.length; i++)
            if (scores[i] > scores[maxIdx]) maxIdx = i;

        Object[] p = PROFILES[maxIdx];
        return new PersonalityResult(
            (String)   p[0],
            (String)   p[1],
            (String)   p[2],
            (String)   p[3],
            (String)   p[4],
            (String[]) p[5],
            scores
        );
    }

    public void applyToUser(User user, PersonalityResult result) {
        user.setPersonalityType(result.type);
        user.setPersonalityScores(result.scores);
        if (result.preferredMoods.length > 0)
            user.setCurrentMood(result.preferredMoods[0]);
    }

    public Map<String, Integer> getScoreBreakdown(int[] scores) {
        int total = Arrays.stream(scores).sum();
        if (total == 0) total = 1;
        String[] names = {"Chill", "Social", "Focus", "Emotional",
                          "Rhythm", "Wanderer", "Power", "Creative"};
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < 8; i++)
            out.put(names[i], scores[i] * 100 / total);
        return out;
    }
}