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

public class QuizPanel extends BasePanel {

    private static final String[][] QUESTIONS = {
        {"When stressed, music helps you...", "Release anger 💢", "Find calm 🌊", "Get energized ⚡", "Feel understood 💙"},
        {"Your ideal playlist is...", "Banging beats 🥁", "Acoustic soulful 🎸", "Lo-fi ambient 🌙", "Epic orchestral 🎻"},
        {"You listen to music most when...", "Working out 🏋", "Studying 📚", "Driving 🚗", "Just relaxing 🛋"},
        {"Your weekend energy is...", "Out dancing 🕺", "Quiet at home 🏠", "Exploring outside 🌲", "Creative projects 🎨"},
        {"A lyric that resonates most?", "'I'm unstoppable' 🔥", "'Take it slow' 🌸", "'Feel the rush' ⚡", "'In my feelings' 💔"},
        {"Your go-to mood when working?", "Hype beats 🎵", "Chill lo-fi 🎧", "Classical focus 🎹", "Nature sounds 🌿"},
        {"Moods as colors — yours today?", "Red (fiery) 🔴", "Blue (calm) 💙", "Yellow (joyful) 🌟", "Purple (creative) 💜"},
        {"You prefer music that's...", "Fast & loud 💥", "Slow & deep 🌊", "Middle ground 🎶", "Totally random 🎲"},
    };

    private static final String[] PERSONALITIES = {
        "The Energizer", "The Dreamer", "The Groove Master", "The Soul Seeker",
        "The Flow State Expert", "The Vibe Curator", "The Beat Scientist", "The Emotional Alchemist"
    };

    private int current = 0;
    private final int[] scores = new int[4];
    private StackPane quizArea;

    public QuizPanel(CommandHistory h) { super(h); }
    @Override public String getPanelId() { return "QUIZ"; }

    @Override
    protected void buildUI() {
        setTop(topBar("🧠 Personality Quiz"));
        quizArea = new StackPane();
        quizArea.setStyle("-fx-background-color:transparent;");
        setCenter(quizArea);
        showIntro();
    }

    private void showIntro() {
        VBox box = new VBox(22);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(480);

        Label icon = new Label("🎵"); icon.setFont(Font.font(54));
        Label title = new Label("Discover Your Music Personality");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(FG);
        title.setWrapText(true); title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label sub = new Label("Answer " + QUESTIONS.length + " quick questions to unlock your personalized listening identity.");
        sub.setStyle("-fx-font-size:14;-fx-text-fill:" + hex(GRAY1) + ";");
        sub.setWrapText(true); sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); sub.setMaxWidth(400);

        Button start = roseBtn("Start Quiz  →");
        start.setMaxWidth(200);
        start.setOnAction(e -> { current = 0; Arrays.fill(scores, 0); showQuestion(); });

        box.getChildren().addAll(icon, title, sub, vsp(10), start);
        animateEntrance(box);
        quizArea.getChildren().setAll(box);
    }

    private void showQuestion() {
        if (current >= QUESTIONS.length) { showResult(); return; }
        String[] q = QUESTIONS[current];

        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(520);
        box.setPadding(new Insets(20));

        // Progress
        double progress = (double) current / QUESTIONS.length;
        StackPane progressBg = new StackPane();
        progressBg.setMaxWidth(440);
        progressBg.setAlignment(Pos.CENTER_LEFT);
        Rectangle bgBar = new Rectangle(440, 6, GRAY3); bgBar.setArcWidth(6); bgBar.setArcHeight(6);
        Rectangle fgBar = new Rectangle(440 * progress, 6,
            new LinearGradient(0,0,1,0,true,CycleMethod.NO_CYCLE, new Stop(0,ACCENT), new Stop(1,ACCENT2)));
        fgBar.setArcWidth(6); fgBar.setArcHeight(6);

        // Animate bar fill
        Timeline tl = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(fgBar.widthProperty(), progress == 0 ? 0 : 440 * ((double)(current-1)/QUESTIONS.length))),
            new KeyFrame(Duration.millis(500), new KeyValue(fgBar.widthProperty(), 440 * progress, Interpolator.EASE_OUT))
        );
        tl.play();
        progressBg.getChildren().addAll(bgBar, fgBar);

        Label prog = new Label("Question " + (current + 1) + " of " + QUESTIONS.length);
        prog.setStyle("-fx-font-size:12;-fx-text-fill:" + hex(GRAY2) + ";");

        Label qLbl = new Label(q[0]);
        qLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        qLbl.setTextFill(FG);
        qLbl.setWrapText(true); qLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); qLbl.setMaxWidth(460);

        VBox options = new VBox(10); options.setMaxWidth(400);
        Color[] colors = {C_RED, C_BLUE, C_ORANGE, C_PURPLE};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button opt = buildOptionButton(q[i + 1], colors[i]);
            opt.setOnAction(e -> { scores[idx % 4]++; current++; transitionQuestion(); });
            // Staggered entrance
            opt.setOpacity(0); opt.setTranslateX(-20);
            final int ii = i;
            PauseTransition pause = new PauseTransition(Duration.millis(ii * 80));
            pause.setOnFinished(ev -> {
                FadeTransition ft = new FadeTransition(Duration.millis(250), opt); ft.setFromValue(0); ft.setToValue(1);
                TranslateTransition tt = new TranslateTransition(Duration.millis(250), opt); tt.setFromX(-20); tt.setToX(0);
                tt.setInterpolator(Interpolator.EASE_OUT);
                new ParallelTransition(ft, tt).play();
            });
            pause.play();
            options.getChildren().add(opt);
        }

        box.getChildren().addAll(prog, progressBg, vsp(8), qLbl, options);
        quizArea.getChildren().setAll(box);
        animateEntrance(box);
    }

    private Button buildOptionButton(String label, Color c) {
        Button b = new Button(label);
        String ch = hex(c);
        String cardHex = hex(CARD);
        String fgHex   = hex(FG);
        String base  = "-fx-background-color:" + cardHex + ";-fx-text-fill:" + fgHex + ";-fx-font-size:14;-fx-border-color:" + ch + "55;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:14 20;-fx-cursor:hand;-fx-alignment:center-left;-fx-max-width:infinity;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);";
        String hover = "-fx-background-color:" + ch + "18;-fx-text-fill:" + fgHex + ";-fx-font-size:14;-fx-border-color:" + ch + "BB;-fx-border-radius:10;-fx-background-radius:10;-fx-padding:14 20;-fx-cursor:hand;-fx-alignment:center-left;-fx-max-width:infinity;-fx-effect:dropshadow(gaussian," + ch + ",8,0.15,0,2);";
        b.setStyle(base); b.setMaxWidth(Double.MAX_VALUE);
        b.setOnMouseEntered(e -> { b.setStyle(hover); animScale(b, 1.02); });
        b.setOnMouseExited(e ->  { b.setStyle(base);  animScale(b, 1.0); });
        return b;
    }

    private void transitionQuestion() {
        if (quizArea.getChildren().isEmpty()) { showQuestion(); return; }
        javafx.scene.Node cur = quizArea.getChildren().get(0);
        FadeTransition ft = new FadeTransition(Duration.millis(140), cur);
        ft.setFromValue(1); ft.setToValue(0);
        ft.setOnFinished(e -> showQuestion());
        ft.play();
    }

    private void showResult() {
        int maxIdx = 0;
        for (int i = 1; i < scores.length; i++) if (scores[i] > scores[maxIdx]) maxIdx = i;
        String personality = PERSONALITIES[maxIdx];
        User u = currentUser();
        if (u != null) u.setPersonalityType(personality);
        // Restart particles with new personality
        startParticles();

        Color[] palettes = {C_RED, C_BLUE, C_ORANGE, C_PURPLE};
        Color accent = palettes[maxIdx];
        String ah = hex(accent);

        VBox box = new VBox(18);
        box.setAlignment(Pos.CENTER); box.setMaxWidth(460); box.setPadding(new Insets(20));

        Label trophy = new Label("🏆"); trophy.setFont(Font.font(52));
        ScaleTransition bounce = new ScaleTransition(Duration.millis(700), trophy);
        bounce.setFromX(0.4); bounce.setToX(1.0); bounce.setFromY(0.4); bounce.setToY(1.0);
        bounce.setInterpolator(Interpolator.EASE_OUT); bounce.play();

        Label result = new Label("You are: " + personality);
        result.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        result.setStyle("-fx-text-fill:" + ah + ";");

        Label desc = new Label("Your unique personality unlocks personalized recommendations. We've set up your profile!");
        desc.setStyle("-fx-font-size:14;-fx-text-fill:" + hex(GRAY1) + ";");
        desc.setWrapText(true); desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER); desc.setMaxWidth(400);

        // Result badge
        HBox badge = new HBox(10);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color:" + ah + "18;-fx-background-radius:12;-fx-border-color:" + ah + "55;-fx-border-radius:12;-fx-border-width:1;-fx-padding:12 24;");
        Label badgeLbl = new Label("🎯  " + personality);
        badgeLbl.setStyle("-fx-font-size:16;-fx-font-weight:bold;-fx-text-fill:" + ah + ";");
        badge.getChildren().add(badgeLbl);

        Button recBtn = roseBtn("✨ See My Recommendations");
        recBtn.setMaxWidth(260); recBtn.setOnAction(e -> navigate("REC"));
        Button retake = outlineBtn("↩ Retake Quiz");
        retake.setMaxWidth(180); retake.setOnAction(e -> showIntro());

        box.getChildren().addAll(trophy, result, badge, desc, vsp(10), recBtn, retake);
        animateEntrance(box);
        quizArea.getChildren().setAll(box);
    }

    @Override public void refresh() { super.refresh(); }
}
