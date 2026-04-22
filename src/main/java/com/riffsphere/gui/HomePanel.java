package com.riffsphere.gui;

import com.riffsphere.models.*;
import com.riffsphere.modules.*;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.*;

import static com.riffsphere.gui.UIFactory.*;

public class HomePanel extends BasePanel {

    public HomePanel(CommandHistory h) { super(h); }
    @Override public String getPanelId() { return "HOME"; }

    @Override
    protected void buildUI() {
        setTop(topBar("Good morning ☀"));
        ScrollPane sc = new ScrollPane();
        sc.setStyle("-fx-background:" + hex(BG2) + ";-fx-background-color:transparent;");
        sc.setFitToWidth(true); sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sc.setContent(buildBody());
        setCenter(sc);
    }

    private VBox buildBody() {
        VBox body = new VBox(30);
        body.setPadding(new Insets(28));
        body.setStyle("-fx-background-color:transparent;");
        body.getChildren().addAll(
            buildHeroSection(),
            buildSection("🔥 Trending Today",    buildSongGrid(db.topRated(8))),
            buildSection("🎯 Quick Picks",         buildMoodGrid()),
            buildSection("📼 Recently Played",      buildSongGrid(getRecentSongs()))
        );
        return body;
    }

    private HBox buildHeroSection() {
        HBox hero = new HBox(20);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.setPadding(new Insets(26));
        // Theme-aware gradient
        boolean light = UIFactory.getTheme() == UIFactory.Theme.LIGHT;
        String gradStyle = light
            ? "-fx-background-color:linear-gradient(to right, #F5EDF4, #FAFAFA);"
            : "-fx-background-color:linear-gradient(to right, #0A1A12, #121212);";
        hero.setStyle(gradStyle + "-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),12,0,0,3);");

        VBox text = new VBox(10); text.setAlignment(Pos.CENTER_LEFT); HBox.setHgrow(text, Priority.ALWAYS);

        Label greeting = new Label("Welcome back!");
        greeting.setStyle("-fx-font-size:13;-fx-text-fill:" + hex(ACCENT) + ";-fx-font-weight:bold;");
        Label name = new Label(currentUser() != null ? currentUser().getUsername() : "Listener");
        name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30)); name.setTextFill(FG);
        Label sub = new Label("What do you want to listen to?");
        sub.setStyle("-fx-font-size:14;-fx-text-fill:" + hex(GRAY1) + ";");

        Button playBtn = roseBtn("▶  Start Listening");
        playBtn.setMaxWidth(200);
        playBtn.setOnAction(e -> {
            List<Song> all = db.getAllSongs();
            if (!all.isEmpty()) playSong(all.get(new Random().nextInt(all.size())));
        });
        text.getChildren().addAll(greeting, name, sub, vsp(8), playBtn);

        StackPane icon = new StackPane();
        icon.setPrefSize(100,100);
        Rectangle bg = new Rectangle(100,100);
        bg.setArcWidth(20); bg.setArcHeight(20);
        bg.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, new Stop(0,ACCENT), new Stop(1,ACCENT2)));
        Label ic = new Label("🎵"); ic.setFont(Font.font(38));
        RotateTransition rt = new RotateTransition(Duration.seconds(10), ic);
        rt.setByAngle(360); rt.setCycleCount(Timeline.INDEFINITE); rt.setInterpolator(Interpolator.LINEAR); rt.play();
        icon.getChildren().addAll(bg, ic);

        hero.getChildren().addAll(text, icon);
        animateEntrance(hero);
        return hero;
    }

    private VBox buildSection(String title, javafx.scene.Node content) {
        VBox sec = new VBox(14);
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17)); t.setTextFill(FG);
        sec.getChildren().addAll(t, content);
        animateEntrance(sec);
        return sec;
    }

    private FlowPane buildSongGrid(List<Song> songs) {
        FlowPane fp = new FlowPane(12, 12);
        Color[] accents = {ACCENT, C_PURPLE, C_ORANGE, C_TEAL, C_RED, C_BLUE, ROSE2, GOLD};
        int idx = 0;
        for (Song s : songs) {
            VBox card = buildSongCard(s, accents[idx++ % accents.length]);
            fp.getChildren().add(card);
        }
        return fp;
    }

    private VBox buildSongCard(Song s, Color accent) {
        VBox card = new VBox(6);
        card.setPrefWidth(150); card.setCursor(javafx.scene.Cursor.HAND);
        String ah = hex(accent);

        StackPane thumb = new StackPane(); thumb.setPrefSize(150, 100);
        Rectangle bg = new Rectangle(150,100);
        bg.setArcWidth(10); bg.setArcHeight(10);
        bg.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, new Stop(0,accent), new Stop(1,accent.darker())));
        Label ic = new Label("♪"); ic.setFont(Font.font(26)); ic.setTextFill(Color.web("#ffffff80"));
        thumb.getChildren().addAll(bg, ic);

        Label title = new Label(s.getTitle());
        title.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + hex(FG) + ";-fx-wrap-text:true;");
        title.setMaxWidth(140);
        Label artist = new Label(s.getArtist());
        artist.setStyle("-fx-font-size:11;-fx-text-fill:" + hex(GRAY1) + ";");

        card.getChildren().addAll(thumb, title, artist);
        card.setPadding(new Insets(0,0,4,0));
        card.setOnMouseEntered(e -> { animScale(card, 1.05); thumb.setEffect(new javafx.scene.effect.DropShadow(10,0,4,accent)); });
        card.setOnMouseExited(e ->  { animScale(card, 1.0);  thumb.setEffect(null); });
        card.setOnMouseClicked(e -> playSong(s));
        return card;
    }

    private FlowPane buildMoodGrid() {
        FlowPane fp = new FlowPane(10,10);
        Object[][] moods = {
            {"😊 Happy",    "happy",    C_LIME},
            {"😢 Sad",      "sad",      C_BLUE},
            {"😤 Angry",    "angry",    C_RED},
            {"😌 Relaxed",  "relaxed",  C_TEAL},
            {"⚡ Energetic","energetic",C_ORANGE},
            {"🎯 Focus",    "focus",    C_PURPLE},
        };
        for (Object[] m : moods) {
            Button b = chipBtn((String)m[0], (Color)m[2]);
            b.setOnAction(e -> navigate("MOOD"));
            fp.getChildren().add(b);
        }
        return fp;
    }

    private List<Song> getRecentSongs() {
        User u = currentUser();
        if (u != null && !u.getHistory().isEmpty())
            return u.getHistory().subList(0, Math.min(8, u.getHistory().size()));
        return db.topRated(8);
    }

    @Override public void refresh() { super.refresh(); buildUI(); }
}
