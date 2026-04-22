package com.riffsphere.gui;

import com.riffsphere.models.*;
import com.riffsphere.modules.*;
import com.riffsphere.utils.UserManager;
import com.riffsphere.gui.UIFactory.Theme;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.*;

import java.util.*;

import static com.riffsphere.gui.UIFactory.*;

public class MusicApp extends Application {

    private final UserManager    um         = UserManager.getInstance();
    private final EventBus       bus        = EventBus.getInstance();
    private final CommandHistory cmdHistory = new CommandHistory();

    private final Map<String, BasePanel> panels = new LinkedHashMap<>();

    private Stage        primaryStage;
    private Scene        scene;
    private BorderPane   rootPane;
    private StackPane    mainPane;
    private VBox         sidebarPane;
    private BorderPane   playerBarPane;
    private String       currentPanelId = "LOGIN";
    
    private String       tempUser = "";
    private String       tempPass = "";

    private MediaPlayer  mediaPlayer;

    // Player widgets (kept as fields so rebuildPlayerBar can reuse)
    private Label     nowPlayingTitle;
    private Label     nowPlayingArtist;
    private Rectangle progressFill;
    private Label     tLeft;
    private AnimationTimer progressTimer;
    private double    progressVal = 0;
    private boolean   isPlaying   = false;
    private Button    playPauseBtn;

    private Label currentNavItem;

    private final double[]    waveHeights = new double[24];
    private final Rectangle[] waveBars    = new Rectangle[24];
    private AnimationTimer    waveTimer;
    private AnimationTimer    floatTimer;

    public Stage getPrimaryStage() { return primaryStage; }

    // ── JavaFX Start ──────────────────────────────────────────────
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("RiffSphere");
        stage.setMinWidth(800); stage.setMinHeight(500);

        for (int i = 0; i < waveHeights.length; i++) waveHeights[i] = Math.random() * 0.5 + 0.1;

        rootPane = new BorderPane();
        mainPane = new StackPane();

        scene = new Scene(rootPane, 960, 560);
        applySceneCSS();
        try {
            buildAppLayout();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }

        KeyCombination undoKey = new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN);
        scene.setOnKeyPressed(e -> {
            if (undoKey.match(e)) UIFactory.showToast(this, cmdHistory.undo(), MAROON);
        });

        bus.subscribe(AppEvent.USER_LOGGED_OUT, ev -> Platform.runLater(this::onLogout));
        bus.subscribe(AppEvent.PLAYLIST_CREATED, ev -> Platform.runLater(this::refreshSidebar));
        bus.subscribe(AppEvent.PLAYLIST_UPDATED, ev -> Platform.runLater(this::refreshSidebar));
        bus.subscribe(AppEvent.PLAYLIST_DELETED, ev -> Platform.runLater(this::refreshSidebar));

        stage.setScene(scene);
        go("LOGIN");
        stage.show();
    }

    // ── Theme Toggle ─────────────────────────────────────────────
    public void toggleTheme() {
        Theme next = UIFactory.getTheme() == Theme.DARK ? Theme.LIGHT : Theme.DARK;
        UIFactory.setTheme(next);
        stopTimers();
        rebuildApp();
    }

    private void applySceneCSS() {
        scene.getStylesheets().clear();
        scene.getStylesheets().add("data:text/css," + UIFactory.buildSceneCSS());
    }

    private void rebuildApp() {
        panels.clear();
        mainPane.getChildren().clear();
        applySceneCSS();
        buildAppLayout();
        go(currentPanelId.equals("LOGIN") ? "LOGIN" : currentPanelId);
    }

    private void buildAppLayout() {
        boolean loggedIn = um.getCurrentUser() != null;

        rootPane.setStyle("-fx-background-color:" + hex(BG) + ";");
        mainPane.setStyle("-fx-background-color:" + hex(BG2) + ";");

        // Login panel
        Region loginRegion = buildLoginPanel();
        loginRegion.setId("LOGIN");
        mainPane.getChildren().add(loginRegion);

        // Screen panels
        registerPanel(new HomePanel(cmdHistory));
        registerPanel(new LibraryPanel(cmdHistory));
        registerPanel(new MoodPanel(cmdHistory));
        registerPanel(new PlaylistPanel(cmdHistory));
        registerPanel(new RecommendationPanel(cmdHistory));
        registerPanel(new QuizPanel(cmdHistory));
        registerPanel(new StatsPanel(cmdHistory));

        sidebarPane   = buildSidebar();
        playerBarPane = buildPlayerBar();

        rootPane.setLeft(sidebarPane);
        rootPane.setCenter(mainPane);
        rootPane.setBottom(playerBarPane);

        boolean showChrome = loggedIn && !currentPanelId.equals("LOGIN");
        sidebarPane.setVisible(showChrome); sidebarPane.setManaged(showChrome);
        playerBarPane.setVisible(showChrome); playerBarPane.setManaged(showChrome);
    }

    private void refreshSidebar() {
        if (rootPane != null && um.isLoggedIn()) {
            sidebarPane = buildSidebar();
            rootPane.setLeft(sidebarPane);
        }
    }

    private void stopTimers() {
        if (progressTimer != null) progressTimer.stop();
        if (waveTimer     != null) waveTimer.stop();
        if (floatTimer    != null) floatTimer.stop();
    }

    // ── Panel registration ────────────────────────────────────────
    private void registerPanel(BasePanel p) {
        p.setApp(this);
        p.setId(p.getPanelId());
        panels.put(p.getPanelId(), p);
        p.setVisible(false);
        mainPane.getChildren().add(p);
    }

    // ── Navigation ────────────────────────────────────────────────
    public void go(String panelId) {
        currentPanelId = panelId;
        mainPane.getChildren().forEach(n -> n.setVisible(panelId.equals(n.getId())));
        BasePanel p = panels.get(panelId);
        if (p != null) {
            p.refresh();
            FadeTransition ft = new FadeTransition(Duration.millis(220), p);
            ft.setFromValue(0.3); ft.setToValue(1.0); ft.play();
        }
    }

    // ── Song Playback ─────────────────────────────────────────────
    public void playSong(Song song) {
        if (song == null) return;
        
        // Stop current playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        nowPlayingTitle.setText(song.getTitle());
        nowPlayingArtist.setText(song.getArtist() + "  ·  " + cap(song.getMood()));
        
        String url = song.getAudioUrl();
        if (url == null || url.isEmpty()) {
            UIFactory.showToast(this, "No audio URL for this song", MAROON);
            isPlaying = false;
            playPauseBtn.setText("▶");
            return;
        }

        try {
            Media media = new Media(url);
            mediaPlayer = new MediaPlayer(media);
            
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (media.getDuration() != null) {
                    double progress = newTime.toMillis() / media.getDuration().toMillis();
                    progressFill.setWidth(380 * progress);
                    int secs = (int) newTime.toSeconds();
                    tLeft.setText(String.format("%d:%02d", secs/60, secs%60));
                }
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                isPlaying = false;
                playPauseBtn.setText("▶");
                progressFill.setWidth(0);
            });

            mediaPlayer.setOnError(() -> {
                String err = mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage() : "Unknown media error";
                System.err.println("--- PLAYBACK ERROR: " + err + " [URL: " + url + "]");
                Platform.runLater(() -> UIFactory.showToast(this, "Streaming Error: " + err, MAROON));
                isPlaying = false;
                playPauseBtn.setText("▶");
            });

            mediaPlayer.setVolume(0.8);
            mediaPlayer.play();
            isPlaying = true;
            playPauseBtn.setText("⏸");

            User u = um.getCurrentUser();
            if (u != null) { u.addToHistory(song); song.incrementPlayCount(); bus.publish(AppEvent.SONG_PLAYED); }
            UIFactory.showToast(this, "▶  " + song.getTitle(), MAROON);
            
        } catch (Exception e) {
            UIFactory.showToast(this, "Streaming Error: " + e.getMessage(), MAROON);
            isPlaying = false;
            playPauseBtn.setText("▶");
        }
    }

    private void onLogout() {
        sidebarPane.setVisible(false); sidebarPane.setManaged(false);
        playerBarPane.setVisible(false); playerBarPane.setManaged(false);
        go("LOGIN");
    }

    // ════════════════════════════════════════════════════════════════
    //  LOGIN PANEL
    // ════════════════════════════════════════════════════════════════
    private Region buildLoginPanel() {
        StackPane outer = new StackPane();
        boolean dark = UIFactory.getTheme() == Theme.DARK;

        LinearGradient bgGrad = dark
            ? new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, new Stop(0,Color.web("#0D0D0D")), new Stop(1,Color.web("#0A1A10")))
            : new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, new Stop(0,Color.web("#FFFFFF")),  new Stop(1,Color.web("#F5EDF0")));
        outer.setBackground(new Background(new BackgroundFill(bgGrad, CornerRadii.EMPTY, Insets.EMPTY)));

        // Floating particles
        Pane particlePane = new Pane();
        particlePane.setMouseTransparent(true);
        outer.getChildren().add(particlePane);

        class Particle { double x,y,dx,dy,w,h,rot; Rectangle rect; }
        List<Particle> particles = new ArrayList<>();
        Random rng = new Random();
        for (int i = 0; i < 16; i++) {
            Particle p = new Particle();
            p.x = rng.nextDouble()*1300; p.y = rng.nextDouble()*800;
            p.dx = (rng.nextDouble()*2-1)*0.45; p.dy = (rng.nextDouble()*2-1)*0.35;
            p.w = rng.nextDouble()*80+40; p.h = rng.nextDouble()*50+25;
            p.rot = rng.nextDouble()*360;
            p.rect = new Rectangle(p.w, p.h);
            p.rect.setArcWidth(12); p.rect.setArcHeight(12);
            p.rect.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE,
                new Stop(0, ACCENT), new Stop(1, ACCENT2)));
            p.rect.setOpacity(dark ? 0.05 : 0.06);
            particlePane.getChildren().add(p.rect);
            particles.add(p);
        }
        floatTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                for (Particle p : particles) {
                    p.x += p.dx; p.y += p.dy; p.rot += 0.1;
                    if (p.x < -120) p.x = 1420; if (p.x > 1420) p.x = -120;
                    if (p.y < -120) p.y = 920;  if (p.y > 920 ) p.y = -120;
                    p.rect.setTranslateX(p.x); p.rect.setTranslateY(p.y); p.rect.setRotate(p.rot);
                }
            }
        };
        if (!um.isLoggedIn()) floatTimer.start();

        // Login card
        VBox card = new VBox(0);
        card.setMaxSize(400, 520);
        card.setAlignment(Pos.TOP_CENTER);
        String cardBg = dark ? "rgba(17,17,17,0.96)" : "rgba(255,255,255,0.97)";
        String border = dark ? "rgba(29,185,84,0.2)" : "rgba(128,0,32,0.2)";
        String shadow = dark ? "0 0 0 1px rgba(29,185,84,0.12)" : "0 4px 36px rgba(128,0,32,0.12)";
        card.setStyle(
            "-fx-background-color:" + cardBg + ";" +
            "-fx-background-radius:22;" +
            "-fx-border-color:" + border + ";" +
            "-fx-border-radius:22;" +
            "-fx-border-width:1.5;" +
            "-fx-padding:44 42 44 42;"
        );
        card.setEffect(new javafx.scene.effect.DropShadow(28, 0, 6, Color.web(dark ? "#00000060" : "#80002025")));

        Label logo = new Label("RiffSphere");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        logo.setTextFill(ACCENT);

        Rectangle accentBar = new Rectangle(48, 3);
        accentBar.setFill(new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE, new Stop(0,ACCENT), new Stop(1,ACCENT2)));
        accentBar.setArcWidth(3); accentBar.setArcHeight(3);

        Label sub = new Label("Music for every mood.");
        sub.setTextFill(GRAY2);
        sub.setStyle("-fx-font-size:13;");

        TextField    userF = loginField("Username");
        PasswordField passF = loginPassField("Password");
        userF.setText(tempUser); passF.setText(tempPass);
        userF.textProperty().addListener((obs,o,n) -> tempUser = n);
        passF.textProperty().addListener((obs,o,n) -> tempPass = n);

        Button loginBtn = roseBtn("Log In");
        Button regBtn   = outlineBtn("Create Account");

        Label statusLbl = new Label(" ");
        statusLbl.setTextFill(ROSE2);
        statusLbl.setStyle("-fx-font-size:12;");

        Label hintLbl = new Label("Default: demo / demo123");
        hintLbl.setTextFill(GRAY2);
        hintLbl.setStyle("-fx-font-size:11;");

        // Theme toggle on login screen
        Button themeToggle = new Button(dark ? "☀ Light Mode" : "🌙 Dark Mode");
        themeToggle.setStyle("-fx-background-color:transparent;-fx-text-fill:" + hex(GRAY2) + ";-fx-font-size:12;-fx-cursor:hand;");
        themeToggle.setOnAction(e -> toggleTheme());

        Runnable doLogin = () -> {
            if (um.login(userF.getText().trim(), passF.getText())) {
                floatTimer.stop();
                sidebarPane.setVisible(true); sidebarPane.setManaged(true);
                playerBarPane.setVisible(true); playerBarPane.setManaged(true);
                isPlaying = true;
                go("HOME");
            } else {
                statusLbl.setText("Incorrect username or password.");
                TranslateTransition shake = new TranslateTransition(Duration.millis(55), card);
                shake.setFromX(0); shake.setToX(11);
                shake.setCycleCount(6); shake.setAutoReverse(true);
                shake.setOnFinished(e -> card.setTranslateX(0));
                shake.play();
            }
        };
        loginBtn.setOnAction(e -> doLogin.run());
        userF.setOnAction(e -> doLogin.run());
        passF.setOnAction(e -> doLogin.run());
        regBtn.setOnAction(e -> showRegisterDialog());

        card.getChildren().addAll(
            logo, vsp(8), accentBar, vsp(8), sub, vsp(26),
            userF, vsp(12), passF, vsp(22),
            loginBtn, vsp(10), regBtn, vsp(16),
            hintLbl, vsp(4), statusLbl, vsp(8), themeToggle
        );

        card.setOpacity(0); card.setTranslateY(28);
        new ParallelTransition(
            buildFade(card, 0, 1, 600),
            buildSlide(card, 28, 0, 600)
        ).play();

        outer.getChildren().add(card);
        return outer;
    }

    private FadeTransition buildFade(javafx.scene.Node n, double from, double to, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), n);
        ft.setFromValue(from); ft.setToValue(to); return ft;
    }
    private TranslateTransition buildSlide(javafx.scene.Node n, double from, double to, int ms) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), n);
        tt.setFromY(from); tt.setToY(to);
        tt.setInterpolator(Interpolator.EASE_OUT); return tt;
    }

    private void showRegisterDialog() {
        TextField uF = new TextField(), eF = new TextField();
        PasswordField pF = new PasswordField();
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Create Account");
        DialogPane dp = dlg.getDialogPane();
        dp.setStyle("-fx-background-color:" + hex(CARD) + ";");
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20));
        Label[] lbls = { new Label("Username:"), new Label("Password:"), new Label("Email (opt):") };
        for (Label l : lbls) l.setTextFill(FG);
        grid.addRow(0, lbls[0], uF); grid.addRow(1, lbls[1], pF); grid.addRow(2, lbls[2], eF);
        dp.setContent(grid);
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.showAndWait().ifPresent(bt -> {
            if (bt != ButtonType.OK) return;
            String u = uF.getText().trim(), p = pF.getText();
            if (u.isBlank() || p.isBlank()) return;
            if (um.register(u, p, eF.getText().trim())) UIFactory.showToast(this, "Account created! Log in now.", ACCENT);
            else UIFactory.showToast(this, "Username already taken.", ROSE2);
        });
    }

    // ════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════════════
    private VBox buildSidebar() {
        boolean light = UIFactory.getTheme() == Theme.LIGHT;
        String sbBg  = hex(SIDEBAR);
        String sbFg  = hex(SIDEBAR_FG);
        String divColor = light ? "rgba(255,255,255,0.15)" : hex(GRAY3);

        VBox side = new VBox();
        side.setPrefWidth(220);
        side.setStyle("-fx-background-color:" + sbBg + ";-fx-border-color:transparent " + divColor + " transparent transparent;-fx-border-width:0 1 0 0;");

        // Logo
        HBox logoRow = new HBox(); logoRow.setPadding(new Insets(22,0,14,22));
        Label logo = new Label("RiffSphere");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        logo.setTextFill(Color.web(sbFg));
        logoRow.getChildren().add(logo);
        side.getChildren().add(logoRow);

        // Theme toggle button (in sidebar when logged in)
        boolean dark = UIFactory.getTheme() == Theme.DARK;
        Button themeBtn = new Button(dark ? "☀  Light Mode" : "🌙  Dark Mode");
        themeBtn.setStyle("-fx-background-color:" + (light ? "rgba(255,255,255,0.12)" : "rgba(255,255,255,0.08)") + ";-fx-text-fill:" + sbFg + ";-fx-font-size:11;-fx-cursor:hand;-fx-background-radius:8;-fx-padding:6 12;");
        themeBtn.setOnAction(e -> toggleTheme());
        themeBtn.setOnMouseEntered(e -> themeBtn.setStyle("-fx-background-color:rgba(255,255,255,0.2);-fx-text-fill:" + sbFg + ";-fx-font-size:11;-fx-cursor:hand;-fx-background-radius:8;-fx-padding:6 12;"));
        themeBtn.setOnMouseExited(e -> themeBtn.setStyle("-fx-background-color:" + (light ? "rgba(255,255,255,0.12)" : "rgba(255,255,255,0.08)") + ";-fx-text-fill:" + sbFg + ";-fx-font-size:11;-fx-cursor:hand;-fx-background-radius:8;-fx-padding:6 12;"));
        VBox.setMargin(themeBtn, new Insets(0,10,10,10));
        side.getChildren().add(themeBtn);

        // Nav items
        String[][] navItems = {
            {"🏠  Home",       "HOME"},
            {"🎵  Library",    "LIBRARY"},
            {"🎭  Mood",       "MOOD"},
            {"💿  Playlists",  "PLAYLIST"},
            {"✨  For You",    "REC"},
            {"🧠  Quiz",       "QUIZ"},
            {"📊  My Stats",   "STATS"},
        };
        for (String[] item : navItems) {
            Label navNode = makeSideNavItem(item[0], item[1]);
            side.getChildren().add(navNode);
            // Restore active state after theme toggle
            if (item[1].equals(currentPanelId)) {
                navNode.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + hex(SIDEBAR_FG) + ";-fx-padding:11 16;-fx-cursor:hand;-fx-background-color:rgba(255,255,255,0.1);-fx-background-radius:8;-fx-border-color:" + (UIFactory.getTheme() == Theme.LIGHT ? hex(ROSE2) : hex(ACCENT)) + " transparent transparent transparent;-fx-border-width:0 0 0 3;-fx-max-width:infinity;");
                currentNavItem = navNode;
            }
        }

        side.getChildren().add(vsp(16));

        // Divider
        Region div = new Region(); div.setMaxSize(180,1);
        div.setStyle("-fx-background-color:" + divColor + ";");
        VBox.setMargin(div, new Insets(0,20,0,20));
        side.getChildren().add(div);
        side.getChildren().add(vsp(10));

        Label plLabel = new Label("YOUR PLAYLISTS");
        plLabel.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:" + sbFg + "88;-fx-padding:0 0 4 22;");
        side.getChildren().add(plLabel);

        String[] pls = {"Happy Vibes ♪","Late Night","Focus Flow","Power Hour","Chill Mix"};
        for (String pl : pls) {
            Label item = new Label("  ♫  " + pl);
            item.setStyle("-fx-font-size:12;-fx-text-fill:" + sbFg + "99;-fx-padding:6 0 6 18;-fx-cursor:hand;");
            item.setOnMouseEntered(e -> item.setStyle("-fx-font-size:12;-fx-text-fill:" + sbFg + ";-fx-padding:6 0 6 18;-fx-cursor:hand;"));
            item.setOnMouseExited(e -> item.setStyle("-fx-font-size:12;-fx-text-fill:" + sbFg + "99;-fx-padding:6 0 6 18;-fx-cursor:hand;"));
            item.setOnMouseClicked(e -> go("PLAYLIST"));
            side.getChildren().add(item);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        side.getChildren().add(spacer);

        Button so = ghostBtn("  ⎋  Sign out");
        so.setOnAction(e -> um.logout());
        VBox.setMargin(so, new Insets(0,0,14,14));
        side.getChildren().add(so);
        return side;
    }

    private Label makeSideNavItem(String label, String target) {
        String sbFg = hex(SIDEBAR_FG);
        String acHex = hex(ACCENT2.equals(ACCENT) ? Color.web("#FFFFFF") : SIDEBAR_FG);
        // In light mode sidebar is burgundy – active highlight is gold/white; dark – active is green
        boolean light = UIFactory.getTheme() == Theme.LIGHT;
        String activeAccent = light ? hex(ROSE2) : hex(ACCENT);

        String normal = "-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + sbFg + "CC;-fx-padding:11 16;-fx-cursor:hand;-fx-max-width:infinity;";
        String hover  = "-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + sbFg + ";-fx-padding:11 16;-fx-cursor:hand;-fx-max-width:infinity;";
        String active = "-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + sbFg + ";-fx-padding:11 16;-fx-cursor:hand;-fx-background-color:rgba(255,255,255,0.1);-fx-background-radius:8;-fx-border-color:" + activeAccent + " transparent transparent transparent;-fx-border-width:0 0 0 3;-fx-max-width:infinity;";

        Label item = new Label(label);
        item.setStyle(normal);
        item.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(item, new Insets(1,8,1,8));
        item.setOnMouseEntered(e -> { if (currentNavItem != item) item.setStyle(hover); });
        item.setOnMouseExited(e ->  { if (currentNavItem != item) item.setStyle(normal); });
        item.setOnMouseClicked(e -> {
            if (currentNavItem != null) currentNavItem.setStyle(normal);
            currentNavItem = item;
            item.setStyle(active);
            go(target);
        });
        return item;
    }

    // ════════════════════════════════════════════════════════════════
    //  PLAYER BAR
    // ════════════════════════════════════════════════════════════════
    private BorderPane buildPlayerBar() {
        boolean light = UIFactory.getTheme() == Theme.LIGHT;
        String pbBg    = hex(PLAYER_BG);
        String border  = hex(GRAY3);
        String fgColor = hex(FG);
        String g1Color = hex(GRAY1);
        String g2Color = hex(GRAY2);

        BorderPane bar = new BorderPane();
        bar.setPrefHeight(90);
        bar.setStyle("-fx-background-color:" + pbBg + ";-fx-border-color:" + border + " transparent transparent transparent;-fx-border-width:1 0 0 0;" + (light ? "-fx-effect:dropshadow(gaussian,rgba(128,0,32,0.1),12,0,0,-3);" : ""));

        // Left: now playing
        HBox left = new HBox(14);
        left.setAlignment(Pos.CENTER_LEFT);
        left.setPrefWidth(280); left.setPadding(new Insets(0,0,0,18));

        StackPane art = new StackPane();
        art.setPrefSize(52,52);
        Rectangle artBg = new Rectangle(52,52);
        artBg.setArcWidth(10); artBg.setArcHeight(10);
        artBg.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, new Stop(0,ACCENT), new Stop(1,ACCENT2)));
        if (light) artBg.setEffect(new javafx.scene.effect.DropShadow(8,0,2,Color.web("#80002044")));
        Label note = new Label("♪"); note.setTextFill(Color.web("#ffffff80")); note.setFont(Font.font(20));
        art.getChildren().addAll(artBg, note);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.4), art);
        pulse.setFromX(1.0); pulse.setToX(1.06); pulse.setFromY(1.0); pulse.setToY(1.06);
        pulse.setCycleCount(Timeline.INDEFINITE); pulse.setAutoReverse(true); pulse.play();

        VBox info = new VBox(3); info.setAlignment(Pos.CENTER_LEFT);
        nowPlayingTitle  = new Label("No song playing");
        nowPlayingTitle.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-text-fill:" + fgColor + ";");
        nowPlayingArtist = new Label("RiffSphere");
        nowPlayingArtist.setStyle("-fx-font-size:12;-fx-text-fill:" + g2Color + ";");
        info.getChildren().addAll(nowPlayingTitle, nowPlayingArtist);
        left.getChildren().addAll(art, info);

        // Center: controls + progress
        VBox center = new VBox(8); center.setAlignment(Pos.CENTER);
        HBox controls = new HBox(20); controls.setAlignment(Pos.CENTER);
        Button prev = playerIconBtn("⏮"), next = playerIconBtn("⏭"), shuffle = playerIconBtn("⇄");
        playPauseBtn = playerPlayBtn();
        playPauseBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                isPlaying = !isPlaying;
                if (isPlaying) mediaPlayer.play();
                else mediaPlayer.pause();
                playPauseBtn.setText(isPlaying ? "⏸" : "▶");
            }
        });
        next.setOnAction(e -> { progressVal = 0; if(progressFill!=null) progressFill.setWidth(0); });
        prev.setOnAction(e -> { progressVal = 0; if(progressFill!=null) progressFill.setWidth(0); });
        controls.getChildren().addAll(shuffle, prev, playPauseBtn, next);

        HBox trackRow = new HBox(8); trackRow.setAlignment(Pos.CENTER); trackRow.setMaxWidth(500);
        tLeft = new Label("0:00"); Label tRight = new Label("3:30");
        tLeft.setStyle("-fx-font-size:11;-fx-text-fill:" + g2Color + ";");
        tRight.setStyle("-fx-font-size:11;-fx-text-fill:" + g2Color + ";");

        StackPane progressStack = new StackPane();
        progressStack.setPrefSize(380, 12); progressStack.setAlignment(Pos.CENTER_LEFT);
        Rectangle bgTrack = new Rectangle(380, 5, GRAY3); bgTrack.setArcWidth(5); bgTrack.setArcHeight(5);
        progressFill = new Rectangle(0, 5,
            new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE, new Stop(0,ACCENT), new Stop(1,ACCENT2)));
        progressFill.setArcWidth(5); progressFill.setArcHeight(5);
        if (light) progressFill.setEffect(new javafx.scene.effect.Glow(0.6));
        progressStack.getChildren().addAll(bgTrack, progressFill);
        progressStack.setCursor(javafx.scene.Cursor.HAND);
        progressStack.setOnMouseClicked(e -> {
            progressVal = (e.getX() / 380.0) * 100;
            progressFill.setWidth(380 * progressVal / 100.0);
        });

        trackRow.getChildren().addAll(tLeft, progressStack, tRight);
        center.getChildren().addAll(controls, trackRow);


        // Right: waveform
        HBox right = new HBox(3); right.setAlignment(Pos.CENTER); right.setPrefWidth(160); right.setPadding(new Insets(0,16,0,0));
        for (int i = 0; i < 24; i++) {
            Rectangle wr = new Rectangle(3, 10,
                new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE, new Stop(0,ACCENT), new Stop(1,ACCENT2)));
            wr.setArcWidth(2); wr.setArcHeight(2);
            waveBars[i] = wr; right.getChildren().add(wr);
        }

        Random rng = new Random();
        if (waveTimer != null) waveTimer.stop();
        waveTimer = new AnimationTimer() {
            private long last = 0;
            @Override public void handle(long now) {
                if (now - last < 80_000_000) return; last = now;
                for (int i = 0; i < 24; i++) {
                    if (isPlaying) {
                        waveHeights[i] += (rng.nextDouble() - 0.5) * 0.22;
                        waveHeights[i] = Math.max(0.05, Math.min(0.95, waveHeights[i]));
                    }
                    waveBars[i].setHeight(36 * waveHeights[i]);
                    waveBars[i].setOpacity(isPlaying ? 1.0 : 0.3);
                }
            }
        };
        waveTimer.start();

        bar.setLeft(left); bar.setCenter(center); bar.setRight(right);
        return bar;
    }

    private String cap(String s) {
        if(s==null||s.isEmpty()) return ""; return Character.toUpperCase(s.charAt(0))+s.substring(1);
    }

    public static void main(String[] args) { launch(args); }
}