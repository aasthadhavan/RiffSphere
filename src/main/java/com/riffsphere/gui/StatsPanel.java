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

public class StatsPanel extends BasePanel {

    public StatsPanel(CommandHistory h) { super(h); }
    @Override public String getPanelId() { return "STATS"; }

    @Override
    protected void buildUI() {
        setTop(topBar("📊 My Stats"));
        ScrollPane sc = new ScrollPane();
        sc.setStyle("-fx-background:" + hex(BG2) + ";-fx-background-color:transparent;");
        sc.setFitToWidth(true); sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sc.setContent(buildBody());
        setCenter(sc);
    }

    private VBox buildBody() {
        VBox body = new VBox(26);
        body.setPadding(new Insets(26));
        body.setStyle("-fx-background-color:transparent;");

        User u = currentUser();
        if (u == null) {
            Label l = new Label("Please log in to view your stats.");
            l.setStyle("-fx-text-fill:" + hex(GRAY2) + ";-fx-font-size:15;");
            body.getChildren().add(l); return body;
        }

        UserStats stats = u.getStats();
        List<Song> history   = u.getHistory();
        List<Song> favorites = u.getFavorites();
        String personality   = u.getPersonalityType();

        String pDisplay = "Unknown";
        if (!personality.isEmpty() && personality.contains(" ")) {
            pDisplay = personality.split(" ")[1];
        } else if (!personality.isEmpty()) {
            pDisplay = personality;
        }

        HBox topCards = new HBox(14); topCards.setAlignment(Pos.CENTER_LEFT);
        Object[][] statData = {
            {"🎵", String.valueOf(stats.getTotalPlays()), "Total Plays",   ACCENT},
            {"❤",  String.valueOf(favorites.size()),      "Favorites",     ROSE2},
            {"🕐", String.valueOf(history.size()),        "Songs Heard",   C_PURPLE},
            {"🧬", pDisplay,                              "Personality", C_ORANGE},
        };
        for (int i = 0; i < statData.length; i++) {
            VBox card = buildStatCard((String)statData[i][0], (String)statData[i][1], (String)statData[i][2], (Color)statData[i][3]);
            card.setOpacity(0); card.setTranslateY(20);
            final int ii = i;
            PauseTransition pause = new PauseTransition(Duration.millis(ii * 90));
            pause.setOnFinished(ev -> {
                FadeTransition ft = new FadeTransition(Duration.millis(280), card); ft.setFromValue(0); ft.setToValue(1);
                TranslateTransition tt = new TranslateTransition(Duration.millis(280), card); tt.setFromY(20); tt.setToY(0); tt.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft,tt).play();
            });
            pause.play();
            topCards.getChildren().add(card);
        }
        body.getChildren().add(topCards);
        body.getChildren().add(buildMoodBar(stats));
        if (!history.isEmpty())   body.getChildren().add(buildSongListSection("🕐 Recently Played",  history.subList(0, Math.min(8, history.size())),   ACCENT));
        if (!favorites.isEmpty()) body.getChildren().add(buildSongListSection("❤ Favorites",          favorites.subList(0, Math.min(8, favorites.size())), ROSE2));
        List<Song> topRated = rec.topRatedByUser(u, 8);
        if (!topRated.isEmpty())  body.getChildren().add(buildSongListSection("⭐ Your Top Rated",   topRated, GOLD));
        return body;
    }

    private VBox buildStatCard(String icon, String value, String label, Color accent) {
        String ah = hex(accent);
        String cardHex = hex(CARD);
        boolean light = UIFactory.getTheme() == UIFactory.Theme.LIGHT;
        String shadow = light ? "dropshadow(gaussian,rgba(128,0,32,0.1),10,0,0,3)" : "dropshadow(gaussian,rgba(0,0,0,0.2),8,0,0,2)";

        VBox card = new VBox(6); card.setPrefWidth(158); card.setPrefHeight(108);
        card.setAlignment(Pos.CENTER); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:14;-fx-border-color:" + ah + "44;-fx-border-radius:14;-fx-border-width:1;-fx-effect:" + shadow + ";");

        card.setOnMouseEntered(e -> { card.setStyle("-fx-background-color:" + hex(CARD_HOV) + ";-fx-background-radius:14;-fx-border-color:" + ah + "99;-fx-border-radius:14;-fx-border-width:1;-fx-effect:dropshadow(gaussian," + ah + ",14,0.2,0,3);"); animScale(card, 1.04); });
        card.setOnMouseExited(e ->  { card.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:14;-fx-border-color:" + ah + "44;-fx-border-radius:14;-fx-border-width:1;-fx-effect:" + shadow + ";"); animScale(card, 1.0); });

        Label iconLbl = new Label(icon); iconLbl.setFont(Font.font(22));
        Label valLbl  = new Label(value); valLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22)); valLbl.setStyle("-fx-text-fill:" + ah + ";");
        Label lbl     = new Label(label); lbl.setStyle("-fx-font-size:12;-fx-text-fill:" + hex(GRAY1) + ";");
        card.getChildren().addAll(iconLbl, valLbl, lbl);

        // Count-up animation
        try {
            int target = Integer.parseInt(value);
            Timeline tl = new Timeline();
            for (int i = 0; i <= 20; i++) {
                final int v = (int)(target * (i / 20.0));
                final int ii = i;
                tl.getKeyFrames().add(new KeyFrame(Duration.millis(ii * 30), ev -> valLbl.setText(String.valueOf(v))));
            }
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(700), ev -> valLbl.setText(String.valueOf(target))));
            tl.play();
        } catch (NumberFormatException ignored) {}
        return card;
    }

    private VBox buildMoodBar(UserStats stats) {
        VBox sec = new VBox(10);
        Label title = new Label("🎭 Mood Distribution");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15)); title.setTextFill(FG);
        sec.getChildren().add(title);

        Map<String, Integer> moodCounts = stats.getMoodCounts();
        if (moodCounts.isEmpty()) {
            Label empty = new Label("No mood data yet. Play some songs!");
            empty.setStyle("-fx-text-fill:" + hex(GRAY2) + ";"); sec.getChildren().add(empty); return sec;
        }
        int maxVal = moodCounts.values().stream().max(Integer::compare).orElse(1);
        Color[] colors = {ACCENT, C_BLUE, C_RED, C_TEAL, C_ORANGE, C_PURPLE};
        int ci = 0;
        for (Map.Entry<String, Integer> e : moodCounts.entrySet()) {
            Color c = colors[ci++ % colors.length];
            double pct = (double) e.getValue() / maxVal;
            HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
            Label moodLbl = new Label(cap(e.getKey())); moodLbl.setStyle("-fx-text-fill:" + hex(GRAY1) + ";-fx-font-size:13;-fx-min-width:70;");
            StackPane barBg = new StackPane(); barBg.setAlignment(Pos.CENTER_LEFT); barBg.setPrefHeight(18);
            Rectangle background = new Rectangle(300, 18, GRAY3); background.setArcWidth(9); background.setArcHeight(9);
            Rectangle fill = new Rectangle(0, 18, c); fill.setArcWidth(9); fill.setArcHeight(9);
            barBg.getChildren().addAll(background, fill);
            Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(fill.widthProperty(), 0)),
                new KeyFrame(Duration.millis(700), new KeyValue(fill.widthProperty(), 300 * pct, Interpolator.EASE_OUT))
            );
            tl.play();
            Label cntLbl = new Label(String.valueOf(e.getValue())); cntLbl.setStyle("-fx-text-fill:" + hex(c) + ";-fx-font-size:12;-fx-font-weight:bold;");
            row.getChildren().addAll(moodLbl, barBg, cntLbl);
            sec.getChildren().add(row);
        }
        return sec;
    }

    private VBox buildSongListSection(String title, List<Song> songs, Color accent) {
        VBox sec = new VBox(8);
        Label t = new Label(title); t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15)); t.setTextFill(FG);
        sec.getChildren().add(t);
        for (int i = 0; i < songs.size(); i++) {
            Song s = songs.get(i);
            HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10,14,10,14)); row.setCursor(javafx.scene.Cursor.HAND); row.setMaxWidth(580);
            String cardHex = hex(CARD);
            row.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),5,0,0,1);");

            Rectangle bar = new Rectangle(3,34,accent); bar.setArcWidth(3); bar.setArcHeight(3);
            VBox info = new VBox(3); HBox.setHgrow(info, Priority.ALWAYS);
            Label tL = new Label(s.getTitle()); tL.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-text-fill:" + hex(FG) + ";");
            Label aL = new Label(s.getArtist() + "  ·  " + s.getGenre()); aL.setStyle("-fx-font-size:11;-fx-text-fill:" + hex(GRAY1) + ";");
            info.getChildren().addAll(tL, aL);
            Label rating = new Label(String.format("%.1f ★", s.getRating())); rating.setStyle("-fx-font-size:12;-fx-text-fill:" + hex(GOLD) + ";");
            row.getChildren().addAll(bar, info, rating);

            row.setOnMouseEntered(e -> { row.setStyle("-fx-background-color:" + hex(CARD_HOV) + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),8,0,0,2);"); animScale(row, 1.01); });
            row.setOnMouseExited(e ->  { row.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),5,0,0,1);"); animScale(row, 1.0); });
            row.setOnMouseClicked(e -> playSong(s));

            row.setOpacity(0);
            PauseTransition p = new PauseTransition(Duration.millis(i * 45));
            p.setOnFinished(ev -> fadeIn(row, 200));
            p.play();
            sec.getChildren().add(row);
        }
        animateEntrance(sec);
        return sec;
    }

    @Override public void refresh() { super.refresh(); buildUI(); }
}
