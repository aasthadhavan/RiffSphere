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

public class RecommendationPanel extends BasePanel {

    private VBox resultsBox;

    public RecommendationPanel(CommandHistory h) { super(h); }
    @Override public String getPanelId() { return "REC"; }

    @Override
    protected void buildUI() {
        setTop(topBar("✨ For You"));
        ScrollPane sc = new ScrollPane();
        sc.setStyle("-fx-background:" + hex(BG2) + ";-fx-background-color:transparent;");
        sc.setFitToWidth(true); sc.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox body = new VBox(22);
        body.setPadding(new Insets(26));
        body.setStyle("-fx-background-color:transparent;");

        Label modeLbl = new Label("Choose recommendation mode:");
        modeLbl.setStyle("-fx-font-size:14;-fx-text-fill:" + hex(GRAY1) + ";");

        HBox modes = new HBox(12);
        Object[][] modeData = {
            {"🎭 By Mood",          "mood",        ACCENT},
            {"🧠 By Personality",   "personality", C_PURPLE},
            {"🔮 Hybrid Mix",       "hybrid",      C_ORANGE},
        };
        for (Object[] md : modeData) {
            Button btn = chipBtn((String)md[0], (Color)md[2]);
            final String mode = (String) md[1];
            final Color  col  = (Color)  md[2];
            btn.setOnAction(e -> loadRecs(mode, col));
            modes.getChildren().add(btn);
        }
        resultsBox = new VBox(12);
        body.getChildren().addAll(modeLbl, modes, vsp(8), resultsBox);
        sc.setContent(body);
        setCenter(sc);
    }

    private void loadRecs(String mode, Color accent) {
        resultsBox.getChildren().clear();
        User u = currentUser();
        if (u == null) {
            Label l = new Label("Please log in to get recommendations.");
            l.setStyle("-fx-text-fill:" + hex(GRAY2) + ";-fx-font-size:14;");
            resultsBox.getChildren().add(l); return;
        }
        Playlist pl = rec.recommend(u, mode, 12);
        List<Song> songs = pl.getSongs();

        Label header = new Label("♫  " + pl.getName());
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18)); header.setTextFill(FG);
        Button shuffleBtn = roseBtn("⇄  Shuffle Play All");
        shuffleBtn.setMaxWidth(200);
        shuffleBtn.setOnAction(e -> { List<Song> cp = new ArrayList<>(songs); Collections.shuffle(cp); if(!cp.isEmpty()) playSong(cp.get(0)); });
        resultsBox.getChildren().addAll(header, shuffleBtn, vsp(8));

        FlowPane grid = new FlowPane(12,12);
        for (int i = 0; i < songs.size(); i++) {
            VBox card = buildRecCard(songs.get(i), accent);
            grid.getChildren().add(card);
            card.setOpacity(0); card.setScaleX(0.88); card.setScaleY(0.88);
            final int ii = i;
            PauseTransition pause = new PauseTransition(Duration.millis(ii * 55));
            pause.setOnFinished(ev -> {
                FadeTransition ft = new FadeTransition(Duration.millis(260), card); ft.setFromValue(0); ft.setToValue(1);
                ScaleTransition st = new ScaleTransition(Duration.millis(260), card);
                st.setFromX(0.88); st.setToX(1.0); st.setFromY(0.88); st.setToY(1.0);
                st.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft,st).play();
            });
            pause.play();
        }
        resultsBox.getChildren().add(grid);
    }

    private VBox buildRecCard(Song s, Color accent) {
        String ah = hex(accent);
        String cardHex = hex(CARD);
        boolean light = UIFactory.getTheme() == UIFactory.Theme.LIGHT;
        String shadow = light
            ? "dropshadow(gaussian,rgba(128,0,32,0.09),12,0,0,3)"
            : "dropshadow(gaussian,rgba(0,0,0,0.25),10,0,0,3)";

        VBox card = new VBox(8);
        card.setPrefWidth(162); card.setPadding(new Insets(14));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:12;-fx-border-color:" + ah + "33;-fx-border-radius:12;-fx-border-width:1;-fx-effect:" + shadow + ";");

        StackPane thumb = new StackPane(); thumb.setPrefSize(134,80);
        Rectangle bg = new Rectangle(134,80);
        bg.setArcWidth(8); bg.setArcHeight(8);
        bg.setFill(new LinearGradient(0,0,1,1,true,CycleMethod.NO_CYCLE, new Stop(0,accent.darker()), new Stop(1,accent)));
        Label ic = new Label("♬"); ic.setFont(Font.font(22)); ic.setTextFill(Color.web("#ffffff70"));
        thumb.getChildren().addAll(bg, ic);

        Label title = new Label(s.getTitle()); title.setStyle("-fx-font-size:12;-fx-font-weight:bold;-fx-text-fill:" + hex(FG) + ";-fx-wrap-text:true;"); title.setMaxWidth(142);
        Label artist = new Label(s.getArtist()); artist.setStyle("-fx-font-size:11;-fx-text-fill:" + hex(GRAY1) + ";");
        Label stars = new Label(String.format("%.1f ★  ·  %s", s.getRating(), cap(s.getMood()))); stars.setStyle("-fx-font-size:11;-fx-text-fill:" + ah + ";");
        card.getChildren().addAll(thumb, title, artist, stars);

        card.setOnMouseEntered(e -> { card.setStyle("-fx-background-color:" + hex(CARD_HOV) + ";-fx-background-radius:12;-fx-border-color:" + ah + "99;-fx-border-radius:12;-fx-border-width:1;-fx-effect:dropshadow(gaussian," + ah + ",14,0.18,0,4);"); animScale(card, 1.04); });
        card.setOnMouseExited(e ->  { card.setStyle("-fx-background-color:" + cardHex + ";-fx-background-radius:12;-fx-border-color:" + ah + "33;-fx-border-radius:12;-fx-border-width:1;-fx-effect:" + shadow + ";"); animScale(card, 1.0); });
        card.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) playSong(s);
            else if (e.getButton().equals(MouseButton.SECONDARY)) showPlaylistContextMenu(card, s, e.getScreenX(), e.getScreenY());
        });
        return card;
    }

    @Override public void refresh() { super.refresh(); if (resultsBox != null) { resultsBox.getChildren().clear(); loadRecs("hybrid", ACCENT); } }
}
