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

public class PlaylistPanel extends BasePanel {

    private VBox playlistListBox;
    private VBox songsBox;

    public PlaylistPanel(CommandHistory h) { super(h); }
    @Override public String getPanelId() { return "PLAYLIST"; }

    @Override
    protected void buildUI() {
        setTop(topBar("💿 Playlists"));
        ScrollPane sc = new ScrollPane();
        sc.setStyle("-fx-background:" + hex(BG2) + ";-fx-background-color:transparent;");
        sc.setFitToWidth(true); sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        HBox body = new HBox(20);
        body.setPadding(new Insets(22));
        body.setStyle("-fx-background-color:transparent;");

        playlistListBox = new VBox(6);
        playlistListBox.setPrefWidth(220);
        playlistListBox.setStyle(
            "-fx-background-color:" + hex(CARD) + ";-fx-background-radius:14;" +
            "-fx-border-color:" + hex(GRAY3) + ";-fx-border-radius:14;-fx-border-width:1;" +
            "-fx-padding:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),10,0,0,2);"
        );

        songsBox = new VBox(10); songsBox.setPadding(new Insets(0,0,0,4));
        HBox.setHgrow(songsBox, Priority.ALWAYS);

        Label placeholder = new Label("← Select a playlist");
        placeholder.setStyle("-fx-font-size:14;-fx-text-fill:" + hex(GRAY2) + ";-fx-font-style:italic;");
        songsBox.getChildren().add(placeholder);

        body.getChildren().addAll(playlistListBox, songsBox);
        sc.setContent(body);
        setCenter(sc);
        refresh();
    }

    @Override
    public void refresh() {
        super.refresh();
        if (playlistListBox == null) return;
        playlistListBox.getChildren().clear();
        playlistListBox.setStyle(
            "-fx-background-color:" + hex(CARD) + ";-fx-background-radius:14;" +
            "-fx-border-color:" + hex(GRAY3) + ";-fx-border-radius:14;-fx-border-width:1;" +
            "-fx-padding:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),10,0,0,2);"
        );

        Label header = new Label("YOUR PLAYLISTS");
        header.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:" + hex(GRAY2) + ";-fx-padding:0 0 8 0;");
        playlistListBox.getChildren().add(header);

        String[][] builtIn = {
            {"Happy Vibes ♪",  "happy",    hex(C_LIME)},
            {"Late Night",      "sad",      hex(C_BLUE)},
            {"Focus Flow",      "focus",    hex(C_PURPLE)},
            {"Power Hour",      "energetic",hex(C_ORANGE)},
            {"Chill Mix",       "relaxed",  hex(C_TEAL)},
        };
        Color[] bandColors = {C_LIME, C_BLUE, C_PURPLE, C_ORANGE, C_TEAL};
        int bi = 0;
        for (String[] pl : builtIn) {
            Color c = bandColors[bi++];
            Label item = buildPlaylistItem(pl[0], c);
            final String mood = pl[1];
            item.setOnMouseClicked(e -> showPlaylist(pl[0], mood, c));
            playlistListBox.getChildren().add(item);
        }
        playlistListBox.getChildren().add(vsp(8));

        User u = currentUser();
        if (u != null && !u.getCustomPlaylists().isEmpty()) {
            Label custHdr = new Label("CUSTOM");
            custHdr.setStyle("-fx-font-size:10;-fx-font-weight:bold;-fx-text-fill:" + hex(GRAY2) + ";-fx-padding:8 0 4 0;");
            playlistListBox.getChildren().add(custHdr);
            u.getCustomPlaylists().forEach((name, pl) -> {
                Label item = buildPlaylistItem(pl.getName(), ACCENT);
                item.setOnMouseClicked(e -> showCustomPlaylist(pl));
                playlistListBox.getChildren().add(item);
            });
        }
        playlistListBox.getChildren().add(vsp(8));
        Button addBtn = outlineBtn("＋ New Playlist");
        addBtn.setOnAction(e -> createPlaylistDialog());
        playlistListBox.getChildren().add(addBtn);
    }

    private Label buildPlaylistItem(String name, Color c) {
        Label item = new Label("▶  " + name);
        String fgStr   = hex(FG);
        String base     = "-fx-font-size:13;-fx-text-fill:" + hex(GRAY1) + ";-fx-padding:9 10;-fx-cursor:hand;-fx-background-radius:8;";
        String hov      = "-fx-font-size:13;-fx-text-fill:" + fgStr + ";-fx-padding:9 10;-fx-cursor:hand;-fx-background-color:" + hex(c) + "18;-fx-background-radius:8;";
        item.setStyle(base); item.setMaxWidth(Double.MAX_VALUE);
        item.setOnMouseEntered(e -> item.setStyle(hov));
        item.setOnMouseExited(e ->  item.setStyle(base));
        return item;
    }

    private void showPlaylist(String name, String mood, Color color) {
        displaySongs(name, db.getByMood(mood), color);
    }
    private void showCustomPlaylist(Playlist pl) { displaySongs(pl.getName(), pl.getSongs(), ACCENT); }

    private void displaySongs(String name, List<Song> songs, Color accent) {
        songsBox.getChildren().clear();

        Label plName = new Label(name);
        plName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20)); plName.setTextFill(FG);
        Label count  = new Label(songs.size() + " songs");
        count.setStyle("-fx-font-size:13;-fx-text-fill:" + hex(GRAY1) + ";");
        Button shuffle = roseBtn("⇄  Shuffle Play");
        shuffle.setMaxWidth(180);
        shuffle.setOnAction(e -> { List<Song> cp = new ArrayList<>(songs); Collections.shuffle(cp); if(!cp.isEmpty()) playSong(cp.get(0)); });

        songsBox.getChildren().addAll(plName, count, vsp(4), shuffle, vsp(8));

        for (int i = 0; i < songs.size(); i++) {
            HBox row = buildSongRow(i+1, songs.get(i), accent);
            songsBox.getChildren().add(row);
            row.setOpacity(0);
            PauseTransition p = new PauseTransition(Duration.millis(i * 40));
            p.setOnFinished(ev -> fadeIn(row, 220));
            p.play();
        }
    }

    private HBox buildSongRow(int num, Song s, Color accent) {
        HBox row = new HBox(12); row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10,14,10,14)); row.setCursor(javafx.scene.Cursor.HAND);
        row.setMaxWidth(600);
        String cardHex = hex(CARD);
        row.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);");

        Label numLbl = new Label(String.valueOf(num));
        numLbl.setStyle("-fx-font-size:12;-fx-text-fill:" + hex(GRAY2) + ";-fx-min-width:22;");
        Rectangle line = new Rectangle(3,36,accent); line.setArcWidth(3); line.setArcHeight(3);
        VBox info = new VBox(3); HBox.setHgrow(info, Priority.ALWAYS);
        Label tl = new Label(s.getTitle()); tl.setStyle("-fx-font-size:13;-fx-font-weight:bold;-fx-text-fill:" + hex(FG) + ";");
        Label al = new Label(s.getArtist() + "  ·  " + s.getGenre()); al.setStyle("-fx-font-size:11;-fx-text-fill:" + hex(GRAY1) + ";");
        info.getChildren().addAll(tl, al);
        Label bpm = new Label(s.getBpm() + " BPM"); bpm.setStyle("-fx-font-size:11;-fx-text-fill:" + hex(GRAY2) + ";");
        Label rating = new Label(String.format("%.1f ★", s.getRating())); rating.setStyle("-fx-font-size:12;-fx-text-fill:" + hex(GOLD) + ";");
        row.getChildren().addAll(numLbl, line, info, bpm, rating);

        row.setOnMouseEntered(e -> { row.setStyle("-fx-background-color:" + hex(CARD_HOV) + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.1),8,0,0,2);"); animScale(row, 1.01); });
        row.setOnMouseExited(e ->  { row.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:10;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,1);"); animScale(row, 1.0); });
        row.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) playSong(s);
            else if (e.getButton().equals(MouseButton.SECONDARY)) showPlaylistContextMenu(row, s, e.getScreenX(), e.getScreenY());
        });
        return row;
    }

    private void createPlaylistDialog() {
        TextInputDialog dlg = new TextInputDialog("New Playlist");
        dlg.setTitle("New Playlist"); dlg.setHeaderText(null); dlg.setContentText("Playlist name:");
        dlg.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;
            PlaylistManager pm = PlaylistManager.getInstance();
            Result<Playlist> res = pm.create(name, "relaxed", currentUser().getUsername());
            if (res.isSuccess()) {
                showToast(res.getMessage(), ACCENT);
                refresh();
            } else {
                showToast(res.getMessage(), MAROON);
            }
        });
    }
}
