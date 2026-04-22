package com.riffsphere.gui;

import com.riffsphere.modules.*;
import com.riffsphere.utils.UserManager;
import com.riffsphere.models.Song;
import com.riffsphere.models.User;
import com.riffsphere.models.Playlist;
import com.riffsphere.modules.Result;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.*;

import static com.riffsphere.gui.UIFactory.*;

public abstract class BasePanel extends StackPane {

    protected final UserManager     um  = UserManager.getInstance();
    protected final SongDatabase    db  = SongDatabase.getInstance();
    protected final Recommender     rec = new Recommender();
    protected final EmotionDetector ed  = new EmotionDetector();
    protected final EventBus        bus = EventBus.getInstance();
    protected final CommandHistory  history;
    protected MusicApp app;

    // Inner layout (all panel content lives here)
    protected final BorderPane layout = new BorderPane();
    // Particle overlay (rendered on top, mouse-transparent)
    protected final Pane particlePane = new Pane();
    // Track timers so we can stop them on navigation
    protected AnimationTimer particleTimer;

    protected BasePanel(CommandHistory history) {
        this.history = history;
        layout.setStyle("-fx-background-color:" + hex(BG2) + ";");
        particlePane.setMouseTransparent(true);
        particlePane.setStyle("-fx-background-color:transparent;");
        getChildren().addAll(layout, particlePane);
        buildUI();
    }

    public void setApp(MusicApp app) { this.app = app; }

    // ── Delegate BorderPane methods ───────────────────────────────
    protected void setTop(Node n)    { layout.setTop(n);    }
    protected void setCenter(Node n) { layout.setCenter(n); }
    protected void setLeft(Node n)   { layout.setLeft(n);   }
    protected void setRight(Node n)  { layout.setRight(n);  }
    protected void setBottom(Node n) { layout.setBottom(n); }

    // ── Template Methods ──────────────────────────────────────────
    protected abstract void buildUI();
    public void refresh() {
        layout.setStyle("-fx-background-color:" + hex(BG2) + ";");
        startParticles();
    }
    public abstract String getPanelId();

    // ── Helpers ───────────────────────────────────────────────────
    protected User currentUser() { return um.getCurrentUser(); }
    protected void navigate(String p) { if (app != null) app.go(p); }
    protected void showToast(String msg) { if (app != null) UIFactory.showToast(app, msg, null); }
    protected void showToast(String msg, Color accent) { if (app != null) UIFactory.showToast(app, msg, accent); }
    protected void playSong(Song song) { if (app != null) app.playSong(song); }
    
    // ── Playlist Context Menu ─────────────────────────────────────
    protected void showPlaylistContextMenu(Node target, Song song, double x, double y) {
        if (currentUser() == null) return;
        
        ContextMenu menu = new ContextMenu();
        menu.setStyle("-fx-background-color:" + hex(CARD) + ";-fx-border-color:" + hex(GRAY3) + ";-fx-border-radius:6;-fx-background-radius:6;");

        // 1. Add to Existing Playlists
        PlaylistManager pm = PlaylistManager.getInstance();
        Collection<Playlist> playlists = pm.getAll();
        
        if (!playlists.isEmpty()) {
            Menu addMenu = new Menu("Add to Playlist");
            for (Playlist pl : playlists) {
                MenuItem item = new MenuItem(pl.getName());
                item.setOnAction(e -> history.push(new AddToPlaylistCommand(pm, pl.getName(), song)));
                addMenu.getItems().add(item);
            }
            menu.getItems().add(addMenu);
        }

        // 2. Create New Playlist
        MenuItem newItem = new MenuItem("+ New Playlist...");
        newItem.setOnAction(e -> showNewPlaylistDialog(song));
        menu.getItems().add(newItem);

        // 3. Toggle Favorite
        MenuItem favItem = new MenuItem(currentUser().isFavorite(song) ? "Unfavorite" : "Favorite");
        favItem.setOnAction(e -> {
            if (currentUser().isFavorite(song)) history.push(new RemoveFavoriteCommand(currentUser(), song));
            else history.push(new AddFavoriteCommand(currentUser(), song));
        });
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(favItem);

        menu.show(target, x, y);
    }

    private void showNewPlaylistDialog(Song song) {
        TextInputDialog dialog = new TextInputDialog("My Awesome Playlist");
        dialog.setTitle("New Playlist");
        dialog.setHeaderText("Create a new playlist and add \"" + song.getTitle() + "\"");
        dialog.setContentText("Name:");
        
        dialog.showAndWait().ifPresent(name -> {
            PlaylistManager pm = PlaylistManager.getInstance();
            Result<Playlist> res = pm.create(name, song.getMood(), currentUser().getUsername());
            if (res.isSuccess()) {
                pm.addSong(name, song);
                showToast("Playlist created!", ACCENT);
            } else {
                showToast(res.getMessage(), MAROON);
            }
        });
    }

    protected Region scrollable(Region content) {
        ScrollPane sc = new ScrollPane(content);
        sc.setStyle("-fx-background:" + hex(BG2) + ";-fx-background-color:transparent;");
        sc.setFitToWidth(true);
        sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return sc;
    }

    // ── Entrance Animation ────────────────────────────────────────
    protected void animateEntrance(Node n) {
        n.setOpacity(0); n.setTranslateY(18);
        FadeTransition ft = new FadeTransition(Duration.millis(350), n); ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(350), n);
        tt.setFromY(18); tt.setToY(0); tt.setInterpolator(Interpolator.EASE_OUT);
        new ParallelTransition(ft, tt).play();
    }

    protected String cap(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ════════════════════════════════════════════════════════════════
    //  PERSONALITY PARTICLE SYSTEM
    // ════════════════════════════════════════════════════════════════
    public void startParticles() {
        stopParticles();
        particlePane.getChildren().clear();

        User u = currentUser();
        String personality = u != null ? u.getPersonalityType().toLowerCase() : "";

        if      (personality.contains("energizer"))  buildSparks();
        else if (personality.contains("dreamer"))    buildBubbles(Color.web("#6AABFF"), 22);
        else if (personality.contains("groove"))     buildNotes();
        else if (personality.contains("soul"))       buildSoulButterflies();
        else if (personality.contains("flow"))       buildLeaves();
        else if (personality.contains("vibe"))       buildGoldSparkles();
        else if (personality.contains("beat"))       buildBeatPulse();
        else if (personality.contains("emotional"))  buildHearts();
        else                                         buildBubbles(ACCENT, 18);
    }

    public void stopParticles() {
        if (particleTimer != null) { particleTimer.stop(); particleTimer = null; }
    }

    // ── Bubbles (Dreamer / Default) ───────────────────────────────
    private void buildBubbles(Color color, int count) {
        Random rng = new Random();
        List<Circle> circles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double r = 4 + rng.nextDouble() * 12;
            Circle c = new Circle(r);
            c.setFill(Color.TRANSPARENT);
            c.setStroke(color);
            c.setStrokeWidth(1.2);
            c.setOpacity(0.08 + rng.nextDouble() * 0.12);
            c.setTranslateX(rng.nextDouble() * 1400);
            c.setTranslateY(rng.nextDouble() * 900);
            circles.add(c);
            particlePane.getChildren().add(c);
        }
        double[] speeds = new double[count];
        double[] dx     = new double[count];
        for (int i = 0; i < count; i++) { speeds[i] = 0.3 + rng.nextDouble() * 0.7; dx[i] = (rng.nextDouble()-0.5)*0.5; }
        particleTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                for (int i = 0; i < circles.size(); i++) {
                    Circle c = circles.get(i);
                    c.setTranslateY(c.getTranslateY() - speeds[i]);
                    c.setTranslateX(c.getTranslateX() + dx[i]);
                    if (c.getTranslateY() < -20) c.setTranslateY(920);
                    if (c.getTranslateX() < -20) c.setTranslateX(1400);
                    if (c.getTranslateX() > 1400) c.setTranslateX(-20);
                }
            }
        };
        particleTimer.start();
    }

    // ── Sparks (Energizer) ────────────────────────────────────────
    private void buildSparks() {
        Random rng = new Random();
        int count = 30;
        List<javafx.scene.shape.Line> sparks = new ArrayList<>();
        double[] vx = new double[count], vy = new double[count];
        Color[] colors = {Color.web("#FF4444"), Color.web("#FF8822"), Color.web("#FFDD00")};
        for (int i = 0; i < count; i++) {
            javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 8 + rng.nextDouble()*12, 0);
            line.setRotate(rng.nextDouble() * 360);
            line.setStroke(colors[rng.nextInt(colors.length)]);
            line.setStrokeWidth(1.5);
            line.setOpacity(0.15 + rng.nextDouble() * 0.2);
            line.setTranslateX(rng.nextDouble() * 1400);
            line.setTranslateY(rng.nextDouble() * 900);
            vx[i] = (rng.nextDouble()-0.5)*3;
            vy[i] = (rng.nextDouble()-0.5)*3;
            sparks.add(line);
            particlePane.getChildren().add(line);
        }
        particleTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                for (int i = 0; i < sparks.size(); i++) {
                    javafx.scene.shape.Line l = sparks.get(i);
                    l.setTranslateX(l.getTranslateX() + vx[i]);
                    l.setTranslateY(l.getTranslateY() + vy[i]);
                    l.setRotate(l.getRotate() + 1.5);
                    if (l.getTranslateX() < -20 || l.getTranslateX() > 1420) vx[i] *= -1;
                    if (l.getTranslateY() < -20 || l.getTranslateY() > 920)  vy[i] *= -1;
                }
            }
        };
        particleTimer.start();
    }

    // ── Musical Notes (Groove Master) ────────────────────────────
    private void buildNotes() {
        Random rng = new Random();
        String[] noteChars = {"♪","♫","♬","♩"};
        int count = 20;
        List<Text> notes = new ArrayList<>();
        double[] speeds = new double[count], dxArr = new double[count], rotSpeed = new double[count];
        Color[] noteColors = {ACCENT, ACCENT2, ROSE2, GOLD};
        for (int i = 0; i < count; i++) {
            Text t = new Text(noteChars[rng.nextInt(noteChars.length)]);
            t.setFont(Font.font(10 + rng.nextDouble() * 16));
            t.setFill(noteColors[rng.nextInt(noteColors.length)]);
            t.setOpacity(0.1 + rng.nextDouble() * 0.15);
            t.setTranslateX(rng.nextDouble() * 1400);
            t.setTranslateY(rng.nextDouble() * 900);
            speeds[i]   = 0.4 + rng.nextDouble() * 0.8;
            dxArr[i]    = (rng.nextDouble()-0.5)*0.6;
            rotSpeed[i] = (rng.nextDouble()-0.5)*1.5;
            notes.add(t);
            particlePane.getChildren().add(t);
        }
        particleTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                for (int i = 0; i < notes.size(); i++) {
                    Text t = notes.get(i);
                    t.setTranslateY(t.getTranslateY() - speeds[i]);
                    t.setTranslateX(t.getTranslateX() + dxArr[i]);
                    t.setRotate(t.getRotate() + rotSpeed[i]);
                    if (t.getTranslateY() < -30) t.setTranslateY(930);
                    if (t.getTranslateX() < -30) t.setTranslateX(1430);
                    if (t.getTranslateX() > 1430) t.setTranslateX(-30);
                }
            }
        };
        particleTimer.start();
    }

    // ── Soul Butterflies ──────────────────────────────────────────
    private void buildSoulButterflies() {
        Random rng = new Random();
        int count = 14;
        List<Text> butterflies = new ArrayList<>();
        double[] speedX = new double[count], speedY = new double[count], phase = new double[count];
        for (int i = 0; i < count; i++) {
            Text t = new Text("🦋");
            t.setFont(Font.font(14 + rng.nextDouble() * 12));
            t.setOpacity(0.12 + rng.nextDouble() * 0.18);
            t.setTranslateX(rng.nextDouble() * 1400);
            t.setTranslateY(rng.nextDouble() * 900);
            speedX[i] = (rng.nextDouble()-0.5)*1.2;
            speedY[i] = (rng.nextDouble()-0.5)*0.8;
            phase[i]  = rng.nextDouble() * Math.PI * 2;
            butterflies.add(t);
            particlePane.getChildren().add(t);
        }
        particleTimer = new AnimationTimer() {
            long start = -1;
            @Override public void handle(long now) {
                if (start < 0) start = now;
                double t = (now - start) / 1_000_000_000.0;
                for (int i = 0; i < butterflies.size(); i++) {
                    Text tf = butterflies.get(i);
                    tf.setTranslateX(tf.getTranslateX() + speedX[i]);
                    tf.setTranslateY(tf.getTranslateY() + Math.sin(t * 1.2 + phase[i]) * 0.7);
                    if (tf.getTranslateX() < -30) tf.setTranslateX(1430);
                    if (tf.getTranslateX() > 1430) tf.setTranslateX(-30);
                    if (tf.getTranslateY() < -30) tf.setTranslateY(930);
                    if (tf.getTranslateY() > 930)  tf.setTranslateY(-30);
                }
            }
        };
        particleTimer.start();
    }

    // ── Leaves (Flow State) ───────────────────────────────────────
    private void buildLeaves() {
        Random rng = new Random();
        int count = 20;
        List<Text> leaves = new ArrayList<>();
        double[] speedX = new double[count], speedY = new double[count], rotSpeed = new double[count], phase = new double[count];
        String[] leafEmojis = {"🍃","🍀","🌿","🍂"};
        for (int i = 0; i < count; i++) {
            Text t = new Text(leafEmojis[rng.nextInt(leafEmojis.length)]);
            t.setFont(Font.font(10 + rng.nextDouble() * 14));
            t.setOpacity(0.1 + rng.nextDouble() * 0.15);
            t.setTranslateX(rng.nextDouble() * 1400);
            t.setTranslateY(rng.nextDouble() * 900);
            speedX[i]   = 0.4 + rng.nextDouble() * 0.8;
            speedY[i]   = (rng.nextDouble()-0.5)*0.5;
            rotSpeed[i] = (rng.nextDouble()-0.5)*2.5;
            phase[i]    = rng.nextDouble() * Math.PI * 2;
            leaves.add(t);
            particlePane.getChildren().add(t);
        }
        particleTimer = new AnimationTimer() {
            long start = -1;
            @Override public void handle(long now) {
                if (start < 0) start = now;
                double tm = (now - start) / 1_000_000_000.0;
                for (int i = 0; i < leaves.size(); i++) {
                    Text tf = leaves.get(i);
                    tf.setTranslateX(tf.getTranslateX() + speedX[i]);
                    tf.setTranslateY(tf.getTranslateY() + speedY[i] + Math.sin(tm + phase[i]) * 0.6);
                    tf.setRotate(tf.getRotate() + rotSpeed[i]);
                    if (tf.getTranslateX() > 1430) tf.setTranslateX(-20);
                    if (tf.getTranslateY() < -30) tf.setTranslateY(930);
                    if (tf.getTranslateY() > 930)  tf.setTranslateY(-30);
                }
            }
        };
        particleTimer.start();
    }

    // ── Gold Sparkles (Vibe Curator) ──────────────────────────────
    private void buildGoldSparkles() {
        Random rng = new Random();
        int count = 35;
        List<Circle> sparkles = new ArrayList<>();
        double[] phase = new double[count], speed = new double[count], dx = new double[count];
        for (int i = 0; i < count; i++) {
            double r = 1.5 + rng.nextDouble() * 3.5;
            Circle c = new Circle(r, GOLD);
            c.setOpacity(0.1 + rng.nextDouble() * 0.15);
            c.setTranslateX(rng.nextDouble() * 1400);
            c.setTranslateY(rng.nextDouble() * 900);
            phase[i] = rng.nextDouble() * Math.PI * 2;
            speed[i] = 0.3 + rng.nextDouble() * 0.6;
            dx[i]    = (rng.nextDouble()-0.5)*0.4;
            sparkles.add(c);
            particlePane.getChildren().add(c);
        }
        particleTimer = new AnimationTimer() {
            long start = -1;
            @Override public void handle(long now) {
                if (start < 0) start = now;
                double t = (now - start) / 1_000_000_000.0;
                for (int i = 0; i < sparkles.size(); i++) {
                    Circle c = sparkles.get(i);
                    c.setTranslateY(c.getTranslateY() - speed[i]);
                    c.setTranslateX(c.getTranslateX() + dx[i]);
                    c.setOpacity(0.05 + 0.12 * Math.abs(Math.sin(t * 2 + phase[i])));
                    if (c.getTranslateY() < -10) c.setTranslateY(910);
                    if (c.getTranslateX() < -10) c.setTranslateX(1410);
                    if (c.getTranslateX() > 1410) c.setTranslateX(-10);
                }
            }
        };
        particleTimer.start();
    }

    // ── Beat Pulse (Beat Scientist) ───────────────────────────────
    private void buildBeatPulse() {
        Random rng = new Random();
        int count = 18;
        List<Circle> dots = new ArrayList<>();
        double[] phase = new double[count];
        for (int i = 0; i < count; i++) {
            double r = 3 + rng.nextDouble() * 6;
            Circle c = new Circle(r, ACCENT);
            c.setOpacity(0.08);
            c.setTranslateX(rng.nextDouble() * 1400);
            c.setTranslateY(rng.nextDouble() * 900);
            phase[i] = rng.nextDouble() * Math.PI * 2;
            dots.add(c);
            particlePane.getChildren().add(c);
        }
        particleTimer = new AnimationTimer() {
            long start = -1;
            @Override public void handle(long now) {
                if (start < 0) start = now;
                double t = (now - start) / 1_000_000_000.0;
                for (int i = 0; i < dots.size(); i++) {
                    Circle c = dots.get(i);
                    double scale = 0.7 + 0.6 * Math.abs(Math.sin(t * 2.4 + phase[i]));
                    c.setScaleX(scale); c.setScaleY(scale);
                    c.setOpacity(0.04 + 0.12 * Math.abs(Math.sin(t * 2.4 + phase[i])));
                }
            }
        };
        particleTimer.start();
    }

    // ── Hearts (Emotional Alchemist) ──────────────────────────────
    private void buildHearts() {
        Random rng = new Random();
        int count = 18;
        List<Text> hearts = new ArrayList<>();
        double[] speed = new double[count], dx = new double[count], phase = new double[count];
        Color[] heartColors = {ROSE2, Color.web("#FF6BA8"), Color.web("#C9A84C")};
        for (int i = 0; i < count; i++) {
            Text t = new Text("♥");
            t.setFont(Font.font(8 + rng.nextDouble() * 16));
            t.setFill(heartColors[rng.nextInt(heartColors.length)]);
            t.setOpacity(0.1 + rng.nextDouble() * 0.15);
            t.setTranslateX(rng.nextDouble() * 1400);
            t.setTranslateY(rng.nextDouble() * 900);
            speed[i] = 0.3 + rng.nextDouble() * 0.7;
            dx[i]    = (rng.nextDouble()-0.5)*0.5;
            phase[i] = rng.nextDouble() * Math.PI * 2;
            hearts.add(t);
            particlePane.getChildren().add(t);
        }
        particleTimer = new AnimationTimer() {
            long start = -1;
            @Override public void handle(long now) {
                if (start < 0) start = now;
                double t = (now - start) / 1_000_000_000.0;
                for (int i = 0; i < hearts.size(); i++) {
                    Text tf = hearts.get(i);
                    tf.setTranslateY(tf.getTranslateY() - speed[i]);
                    tf.setTranslateX(tf.getTranslateX() + dx[i] + Math.sin(t + phase[i]) * 0.4);
                    double sc = 0.8 + 0.25 * Math.abs(Math.sin(t * 1.8 + phase[i]));
                    tf.setScaleX(sc); tf.setScaleY(sc);
                    if (tf.getTranslateY() < -20) tf.setTranslateY(920);
                    if (tf.getTranslateX() < -20) tf.setTranslateX(1420);
                    if (tf.getTranslateX() > 1420) tf.setTranslateX(-20);
                }
            }
        };
        particleTimer.start();
    }
}
