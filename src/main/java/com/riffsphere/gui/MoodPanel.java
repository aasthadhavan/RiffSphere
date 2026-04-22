package com.riffsphere.gui;

import com.riffsphere.models.*;
import com.riffsphere.modules.*;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.*;

import static com.riffsphere.gui.UIFactory.*;

public class MoodPanel extends BasePanel {

    private String selectedMood = null;
    private VBox resultsBox;
    private Label selectedLabel;

    public MoodPanel(CommandHistory h) { super(h); }
    @Override public String getPanelId() { return "MOOD"; }

    @Override
    protected void buildUI() {
        setTop(topBar("🎭 Mood Mixer"));
        ScrollPane sc = new ScrollPane();
        sc.setStyle("-fx-background:" + hex(BG2) + ";-fx-background-color:transparent;");
        sc.setFitToWidth(true); sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox body = new VBox(22);
        body.setPadding(new Insets(26));
        body.setStyle("-fx-background-color:transparent;");

        Label sub = new Label("Select your current mood and discover tailored music.");
        sub.setStyle("-fx-font-size:14;-fx-text-fill:" + hex(GRAY1) + ";");

        selectedLabel = new Label("No mood selected");
        selectedLabel.setStyle("-fx-font-size:13;-fx-text-fill:" + hex(GRAY2) + ";-fx-font-style:italic;");

        FlowPane moodGrid = buildMoodGrid();
        resultsBox = new VBox(10);

        body.getChildren().addAll(sub, moodGrid, selectedLabel, resultsBox);
        sc.setContent(body);
        setCenter(sc);
    }

    private FlowPane buildMoodGrid() {
        FlowPane fp = new FlowPane(14,14);
        Object[][] moods = {
            {"😊  Happy",    "happy",    C_LIME,   "Uplifting beats for great days"},
            {"😢  Sad",      "sad",      C_BLUE,   "Gentle melodies for deep feelings"},
            {"😤  Angry",    "angry",    C_RED,    "Power tracks to release tension"},
            {"😌  Relaxed",  "relaxed",  C_TEAL,   "Calm vibes for unwinding"},
            {"⚡  Energetic","energetic",C_ORANGE, "High-energy bangers"},
            {"🎯  Focus",    "focus",    C_PURPLE, "Concentration boosting ambience"},
        };
        for (Object[] m : moods) {
            Color c = (Color) m[2];
            String ch = hex(c);
            VBox tile = new VBox(8);
            tile.setPrefSize(158, 108);
            tile.setAlignment(Pos.CENTER); tile.setCursor(javafx.scene.Cursor.HAND);
            tile.setStyle("-fx-background-color:" + ch + "18;-fx-background-radius:14;-fx-border-color:" + ch + "55;-fx-border-radius:14;-fx-border-width:1;");
            tile.setPadding(new Insets(10));

            Label icon = new Label(((String)m[0]).split("  ")[0]); icon.setFont(Font.font(26));
            Label name = new Label(((String)m[0]).split("  ")[1]);
            name.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-text-fill:" + ch + ";");
            Label desc = new Label((String)m[3]);
            desc.setStyle("-fx-font-size:10;-fx-text-fill:" + hex(GRAY2) + ";-fx-wrap-text:true;-fx-text-alignment:center;");
            desc.setMaxWidth(148);
            tile.getChildren().addAll(icon, name, desc);

            final String moodId = (String) m[1];
            tile.setOnMouseEntered(e -> { tile.setStyle("-fx-background-color:" + ch + "30;-fx-background-radius:14;-fx-border-color:" + ch + "BB;-fx-border-radius:14;-fx-border-width:1.5;"); animScale(tile, 1.06); });
            tile.setOnMouseExited(e ->  { tile.setStyle("-fx-background-color:" + ch + "18;-fx-background-radius:14;-fx-border-color:" + ch + "55;-fx-border-radius:14;-fx-border-width:1;");  animScale(tile, 1.0);});
            tile.setOnMouseClicked(e -> selectMood(moodId, c));
            fp.getChildren().add(tile);
        }
        return fp;
    }

    private void selectMood(String mood, Color color) {
        selectedMood = mood;
        User u = currentUser();
        if (u != null) u.setCurrentMood(mood);
        selectedLabel.setText("Showing songs for: " + cap(mood));
        selectedLabel.setStyle("-fx-font-size:13;-fx-text-fill:" + hex(color) + ";-fx-font-weight:bold;");
        showResults(mood, color);
    }

    private void showResults(String mood, Color color) {
        resultsBox.getChildren().clear();
        List<Song> songs = db.getByMood(mood);
        if (songs.isEmpty()) {
            Label empty = new Label("No songs found for this mood.");
            empty.setStyle("-fx-text-fill:" + hex(GRAY2) + ";");
            resultsBox.getChildren().add(empty); return;
        }
        Label header = new Label("🎵 " + songs.size() + " songs for " + cap(mood));
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16)); header.setTextFill(FG);
        resultsBox.getChildren().add(header);

        FlowPane fp = new FlowPane(12,12);
        for (Song s : songs) {
            HBox row = new HBox(12);
            row.setPadding(new Insets(12,14,12,14)); row.setAlignment(Pos.CENTER_LEFT);
            row.setCursor(javafx.scene.Cursor.HAND); row.setPrefWidth(340);
            String ch = hex(color);
            String cardHex = hex(CARD);
            row.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),6,0,0,2);");

            Rectangle bar = new Rectangle(3,38,color); bar.setArcWidth(3); bar.setArcHeight(3);
            VBox info = new VBox(3); HBox.setHgrow(info, Priority.ALWAYS);
            Label t = new Label(s.getTitle()); t.setStyle("-fx-font-weight:bold;-fx-font-size:13;-fx-text-fill:" + hex(FG) + ";");
            Label a = new Label(s.getArtist() + "  ·  " + s.getBpm() + " BPM"); a.setStyle("-fx-font-size:11;-fx-text-fill:" + hex(GRAY1) + ";");
            info.getChildren().addAll(t, a);

            Label rating = new Label(String.format("%.1f ★", s.getRating()));
            rating.setStyle("-fx-font-size:12;-fx-text-fill:" + hex(GOLD) + ";");
            row.getChildren().addAll(bar, info, rating);

            row.setOnMouseEntered(ev -> { row.setStyle("-fx-background-color:" + hex(CARD_HOV) + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian," + ch + ",8,0.1,0,2);"); animScale(row, 1.02); });
            row.setOnMouseExited(ev ->  { row.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),6,0,0,2);"); animScale(row, 1.0); });
            row.setOnMouseClicked(ev -> {
                if (ev.getButton().equals(MouseButton.PRIMARY)) playSong(s);
                else if (ev.getButton().equals(MouseButton.SECONDARY)) showPlaylistContextMenu(row, s, ev.getScreenX(), ev.getScreenY());
            });
            fp.getChildren().add(row);
        }
        resultsBox.getChildren().add(fp);
        animateEntrance(resultsBox);
    }

    @Override public void refresh() { super.refresh(); }
}
