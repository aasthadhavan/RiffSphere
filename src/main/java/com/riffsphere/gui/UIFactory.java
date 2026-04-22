package com.riffsphere.gui;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public final class UIFactory {

    public enum Theme { DARK, LIGHT }
    private static Theme currentTheme = Theme.DARK;

    // ── Dynamic Color Tokens (swapped on theme change) ────────────
    public static Color BG, BG2, BG3, CARD, CARD_HOV;
    public static Color ACCENT, ACCENT2, MAROON, ROSE, ROSE2, GOLD;
    public static Color FG, GRAY1, GRAY2, GRAY3;
    public static Color SIDEBAR, SIDEBAR_FG, PLAYER_BG;
    public static Color WHITE; // alias for FG (foreground text)
    // Extra accent palette
    public static Color C_PURPLE, C_ORANGE, C_TEAL, C_RED, C_BLUE, C_LIME;

    static { applyPalette(Theme.DARK); }

    public static void setTheme(Theme t) {
        currentTheme = t;
        applyPalette(t);
    }
    public static Theme getTheme() { return currentTheme; }

    private static void applyPalette(Theme t) {
        if (t == Theme.DARK) {
            BG          = Color.web("#000000");
            BG2         = Color.web("#121212");
            BG3         = Color.web("#181818");
            CARD        = Color.web("#1E1E1E");
            CARD_HOV    = Color.web("#282828");
            ACCENT      = Color.web("#1DB954");
            ACCENT2     = Color.web("#1ED760");
            MAROON      = ACCENT;
            ROSE        = ACCENT2;
            ROSE2       = Color.web("#FF6B6B");
            GOLD        = Color.web("#FFD60A");
            FG          = Color.web("#FFFFFF");
            WHITE       = FG;
            GRAY1       = Color.web("#B3B3B3");
            GRAY2       = Color.web("#727272");
            GRAY3       = Color.web("#282828");
            SIDEBAR     = Color.web("#000000");
            SIDEBAR_FG  = Color.web("#FFFFFF");
            PLAYER_BG   = Color.web("#090909");
        } else {
            // Luxury White + Burgundy
            BG          = Color.web("#FAFAFA");
            BG2         = Color.web("#FFFFFF");
            BG3         = Color.web("#F5EEF0");
            CARD        = Color.web("#FFFFFF");
            CARD_HOV    = Color.web("#FDF5F7");
            ACCENT      = Color.web("#800020");
            ACCENT2     = Color.web("#9B2335");
            MAROON      = ACCENT;
            ROSE        = ACCENT2;
            ROSE2       = Color.web("#C9A84C");  // soft gold
            GOLD        = Color.web("#C9A84C");
            FG          = Color.web("#1A0810");  // near-black text
            WHITE       = FG;
            GRAY1       = Color.web("#4A2030");  // dark wine for secondary text
            GRAY2       = Color.web("#9A7080");  // muted rose
            GRAY3       = Color.web("#EAD8DE");  // light divider
            SIDEBAR     = Color.web("#800020");  // burgundy sidebar
            SIDEBAR_FG  = Color.web("#FFFFFF");  // white text on sidebar
            PLAYER_BG   = Color.web("#FFFFFF");
        }
        // Shared accent palette — adjust brightness for theme
        C_PURPLE = t == Theme.DARK ? Color.web("#9B59B6") : Color.web("#7B3F9E");
        C_ORANGE = t == Theme.DARK ? Color.web("#E67E22") : Color.web("#C96A10");
        C_TEAL   = t == Theme.DARK ? Color.web("#16A085") : Color.web("#0D7A65");
        C_RED    = t == Theme.DARK ? Color.web("#E74C3C") : Color.web("#C0392B");
        C_BLUE   = t == Theme.DARK ? Color.web("#3498DB") : Color.web("#1A6FAB");
        C_LIME   = t == Theme.DARK ? Color.web("#27AE60") : Color.web("#1A8A48");
    }

    private UIFactory() {}

    // ── Hex helper ────────────────────────────────────────────────
    public static String hex(Color c) {
        if (c == null) return "#000000";
        return String.format("#%02X%02X%02X",
            (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    // ── Global scene CSS ─────────────────────────────────────────
    public static String buildSceneCSS() {
        boolean dark = currentTheme == Theme.DARK;
        String bg     = hex(BG2);
        String card   = hex(CARD);
        String fg     = hex(FG);
        String g1     = hex(GRAY1);
        String g2     = hex(GRAY2);
        String g3     = hex(GRAY3);
        String acc    = hex(ACCENT);
        return
            "*{-fx-font-family:'Segoe UI',sans-serif;}" +
            ".scroll-pane>.viewport{-fx-background-color:transparent;}" +
            ".scroll-bar:vertical .track{-fx-background-color:" + (dark ? "#1E1E1E" : "#F5EEF0") + ";}" +
            ".scroll-bar:vertical .thumb{-fx-background-color:" + g3 + ";-fx-background-radius:4;}" +
            ".scroll-bar:horizontal{-fx-opacity:0;}" +
            ".table-view .column-header-background{-fx-background-color:" + bg + ";}" +
            ".table-view .column-header{-fx-background-color:" + bg + ";-fx-text-fill:" + g2 + ";}" +
            ".table-row-cell{-fx-background-color:" + bg + ";-fx-border-color:transparent;}" +
            ".table-row-cell:selected{-fx-background-color:" + acc + "20;}" +
            ".table-row-cell:hover{-fx-background-color:" + acc + "10;}" +
            ".combo-box{-fx-background-color:" + card + ";-fx-text-fill:" + fg + ";-fx-border-color:" + g3 + ";}" +
            ".combo-box .list-cell{-fx-background-color:" + card + ";-fx-text-fill:" + fg + ";}" +
            ".combo-box-popup .list-view{-fx-background-color:" + card + ";}" +
            ".combo-box-popup .list-cell{-fx-text-fill:" + fg + ";-fx-background-color:" + card + ";}" +
            ".combo-box-popup .list-cell:hover{-fx-background-color:" + g3 + ";}" +
            ".slider .track{-fx-background-color:" + g3 + ";}" +
            ".slider .thumb{-fx-background-color:" + acc + ";}";
    }

    // ── Buttons ───────────────────────────────────────────────────

    public static Button roseBtn(String text) {
        Button b = new Button(text);
        boolean dark = currentTheme == Theme.DARK;
        String textColor = dark ? "#000000" : "#FFFFFF";
        String base  = "-fx-background-color:" + hex(ACCENT) + ";-fx-text-fill:" + textColor + ";-fx-font-weight:bold;-fx-background-radius:24;-fx-padding:12 24;-fx-cursor:hand;-fx-font-size:13px;";
        String hover = "-fx-background-color:" + hex(ACCENT2) + ";-fx-text-fill:" + textColor + ";-fx-font-weight:bold;-fx-background-radius:24;-fx-padding:12 24;-fx-cursor:hand;-fx-font-size:13px;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> { b.setStyle(hover); animScale(b, 1.04); });
        b.setOnMouseExited(e ->  { b.setStyle(base);  animScale(b, 1.0);  });
        b.setOnMousePressed(e ->  animScale(b, 0.97));
        b.setOnMouseReleased(e -> animScale(b, 1.04));
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    public static Button outlineBtn(String text) {
        Button b = new Button(text);
        String fg   = hex(FG);
        String g2   = hex(GRAY2);
        String g1   = hex(GRAY1);
        String base  = "-fx-background-color:transparent;-fx-text-fill:" + fg + ";-fx-font-weight:bold;-fx-border-color:" + g2 + ";-fx-border-radius:24;-fx-background-radius:24;-fx-padding:11 24;-fx-cursor:hand;-fx-font-size:13px;";
        String hover = "-fx-background-color:" + hex(ACCENT) + "18;-fx-text-fill:" + fg + ";-fx-font-weight:bold;-fx-border-color:" + hex(ACCENT) + ";-fx-border-radius:24;-fx-background-radius:24;-fx-padding:11 24;-fx-cursor:hand;-fx-font-size:13px;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> { b.setStyle(hover); animScale(b, 1.03); });
        b.setOnMouseExited(e ->  { b.setStyle(base);  animScale(b, 1.0);  });
        b.setMaxWidth(Double.MAX_VALUE);
        return b;
    }

    public static Button chipBtn(String text, Color mc) {
        Button b = new Button(text);
        String mh = hex(mc);
        String base  = "-fx-background-color:" + mh + "22;-fx-text-fill:" + mh + ";-fx-font-weight:bold;-fx-border-color:" + mh + "77;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:5 14;-fx-cursor:hand;-fx-font-size:12px;";
        String hover = "-fx-background-color:" + mh + "44;-fx-text-fill:" + mh + ";-fx-font-weight:bold;-fx-border-color:" + mh + "CC;-fx-border-radius:20;-fx-background-radius:20;-fx-padding:5 14;-fx-cursor:hand;-fx-font-size:12px;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> { b.setStyle(hover); animScale(b, 1.06); });
        b.setOnMouseExited(e ->  { b.setStyle(base);  animScale(b, 1.0);  });
        return b;
    }

    public static Button ghostBtn(String text) {
        Button b = new Button(text);
        String sfg = hex(SIDEBAR_FG);
        b.setStyle("-fx-background-color:transparent;-fx-text-fill:" + sfg + "AA;-fx-padding:8 16;-fx-cursor:hand;-fx-font-size:13px;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color:" + sfg + "20;-fx-text-fill:" + sfg + ";-fx-padding:8 16;-fx-cursor:hand;-fx-font-size:13px;-fx-background-radius:8;"));
        b.setOnMouseExited(e ->  b.setStyle("-fx-background-color:transparent;-fx-text-fill:" + sfg + "AA;-fx-padding:8 16;-fx-cursor:hand;-fx-font-size:13px;"));
        return b;
    }

    public static Button playerIconBtn(String icon) {
        boolean dark = currentTheme == Theme.DARK;
        String fgColor  = dark ? hex(GRAY1) : hex(GRAY1);
        String fgHover  = dark ? "#FFFFFF"  : hex(ACCENT);
        Button b = new Button(icon);
        b.setStyle("-fx-background-color:transparent;-fx-text-fill:" + fgColor + ";-fx-font-size:18px;-fx-cursor:hand;");
        b.setOnMouseEntered(e -> { b.setStyle("-fx-background-color:transparent;-fx-text-fill:" + fgHover + ";-fx-font-size:18px;-fx-cursor:hand;"); animScale(b, 1.2); });
        b.setOnMouseExited(e ->  { b.setStyle("-fx-background-color:transparent;-fx-text-fill:" + fgColor + ";-fx-font-size:18px;-fx-cursor:hand;"); animScale(b, 1.0); });
        return b;
    }

    public static Button playerPlayBtn() {
        boolean dark = currentTheme == Theme.DARK;
        String baseBg  = dark ? "#FFFFFF" : hex(ACCENT);
        String baseText= dark ? "#000000" : "#FFFFFF";
        String hovBg   = dark ? hex(ACCENT2) : hex(ACCENT2);
        String hovText = dark ? "#000000"    : "#FFFFFF";
        Button b = new Button("▶");
        String base  = "-fx-background-color:" + baseBg + ";-fx-text-fill:" + baseText + ";-fx-font-size:14px;-fx-background-radius:22;-fx-min-width:44;-fx-min-height:44;-fx-cursor:hand;-fx-effect:dropshadow(gaussian," + hex(ACCENT) + ",8,0.4,0,2);";
        String hover = "-fx-background-color:" + hovBg  + ";-fx-text-fill:" + hovText  + ";-fx-font-size:14px;-fx-background-radius:24;-fx-min-width:48;-fx-min-height:48;-fx-cursor:hand;-fx-effect:dropshadow(gaussian," + hex(ACCENT) + ",14,0.6,0,3);";
        b.setStyle(base);
        b.setOnMouseEntered(e -> { b.setStyle(hover); animScale(b, 1.1); });
        b.setOnMouseExited(e ->  { b.setStyle(base);  animScale(b, 1.0); });
        // Pulse animation
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.4), b);
        pulse.setFromX(1.0); pulse.setToX(1.07);
        pulse.setFromY(1.0); pulse.setToY(1.07);
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
        return b;
    }

    // ── Text Fields ───────────────────────────────────────────────

    public static TextField loginField(String placeholder) {
        TextField f = new TextField();
        f.setPromptText(placeholder);
        String cardHex  = hex(CARD);
        String g2       = hex(GRAY2);
        String g3       = hex(GRAY3);
        String fgHex    = hex(FG);
        String accHex   = hex(ACCENT);
        String base    = "-fx-background-color:" + cardHex + ";-fx-text-fill:" + fgHex + ";-fx-prompt-text-fill:" + g2 + ";-fx-border-color:" + g3 + ";-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12 14;-fx-font-size:14px;";
        String focused = "-fx-background-color:" + cardHex + ";-fx-text-fill:" + fgHex + ";-fx-prompt-text-fill:" + g2 + ";-fx-border-color:" + accHex + ";-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12 14;-fx-font-size:14px;";
        f.setStyle(base);
        f.focusedProperty().addListener((obs,o,n) -> f.setStyle(n ? focused : base));
        return f;
    }

    public static PasswordField loginPassField(String placeholder) {
        PasswordField f = new PasswordField();
        f.setPromptText(placeholder);
        String cardHex  = hex(CARD);
        String g2       = hex(GRAY2);
        String g3       = hex(GRAY3);
        String fgHex    = hex(FG);
        String accHex   = hex(ACCENT);
        String base    = "-fx-background-color:" + cardHex + ";-fx-text-fill:" + fgHex + ";-fx-prompt-text-fill:" + g2 + ";-fx-border-color:" + g3 + ";-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12 14;-fx-font-size:14px;";
        String focused = "-fx-background-color:" + cardHex + ";-fx-text-fill:" + fgHex + ";-fx-prompt-text-fill:" + g2 + ";-fx-border-color:" + accHex + ";-fx-border-radius:8;-fx-background-radius:8;-fx-padding:12 14;-fx-font-size:14px;";
        f.setStyle(base);
        f.focusedProperty().addListener((obs,o,n) -> f.setStyle(n ? focused : base));
        return f;
    }

    public static TextField searchField(String placeholder) { return loginField(placeholder); }

    // ── Labels ────────────────────────────────────────────────────

    public static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:" + hex(FG) + ";");
        return l;
    }

    public static Label topBarLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:" + hex(FG) + ";");
        return l;
    }

    // ── Top Bar ───────────────────────────────────────────────────

    public static BorderPane topBar(String title) {
        BorderPane bar = new BorderPane();
        bar.setStyle("-fx-background-color:" + hex(BG2) + ";-fx-border-color:transparent transparent " + hex(GRAY3) + " transparent;-fx-border-width:0 0 1 0;");
        bar.setMinHeight(58);
        bar.setPadding(new Insets(0,28,0,28));
        Label l = topBarLabel(title);
        BorderPane.setAlignment(l, Pos.CENTER_LEFT);
        bar.setLeft(l);
        return bar;
    }

    // ── Spacers ───────────────────────────────────────────────────

    public static Region vsp(double h) {
        Region r = new Region(); r.setMinHeight(h); r.setMaxHeight(h); return r;
    }
    public static Region hsp(double w) {
        Region r = new Region(); r.setMinWidth(w); r.setMaxWidth(w); return r;
    }

    // ── Card factory ──────────────────────────────────────────────

    public static VBox card(Color accent) {
        VBox c = new VBox(10);
        String ah = hex(accent);
        boolean light = currentTheme == Theme.LIGHT;
        String shadow = light ? "dropshadow(gaussian,rgba(128,0,32,0.13),12,0,0,4)" : "dropshadow(gaussian,rgba(0,0,0,0.35),10,0,0,3)";
        String baseStyle = "-fx-background-color:" + hex(CARD) + ";-fx-background-radius:14;-fx-border-color:" + ah + "33;-fx-border-radius:14;-fx-border-width:1;-fx-padding:16;-fx-effect:" + shadow + ";";
        String hoverStyle = "-fx-background-color:" + hex(CARD_HOV) + ";-fx-background-radius:14;-fx-border-color:" + ah + "99;-fx-border-radius:14;-fx-border-width:1.5;-fx-padding:16;-fx-effect:dropshadow(gaussian," + ah + ",16,0.2,0,4);";
        c.setStyle(baseStyle);
        c.setOnMouseEntered(e -> { c.setStyle(hoverStyle); animScale(c, 1.025); });
        c.setOnMouseExited(e ->  { c.setStyle(baseStyle);  animScale(c, 1.0);   });
        return c;
    }

    // ── Toast ─────────────────────────────────────────────────────

    public static void showToast(MusicApp parent, String message, Color accent) {
        if (parent == null || parent.getPrimaryStage() == null) return;
        boolean light = currentTheme == Theme.LIGHT;
        String textColor = light ? "#FFFFFF" : "#000000";
        Popup popup = new Popup();
        Label lbl = new Label("  " + message + "  ");
        lbl.setStyle("-fx-background-color:" + hex(accent != null ? accent : ACCENT) + ";-fx-text-fill:" + textColor + ";-fx-font-weight:bold;-fx-padding:10 18;-fx-background-radius:8;-fx-font-size:13px;-fx-effect:dropshadow(gaussian," + hex(accent != null ? accent : ACCENT) + ",12,0.3,0,3);");
        lbl.setOpacity(0);
        popup.getContent().add(lbl);
        Window stage = parent.getPrimaryStage();
        popup.show(stage);
        lbl.applyCss(); lbl.layout();
        popup.setX(stage.getX() + (stage.getWidth() - lbl.getWidth()) / 2);
        popup.setY(stage.getY() + stage.getHeight() - 115);
        FadeTransition fi = new FadeTransition(Duration.millis(200), lbl); fi.setFromValue(0); fi.setToValue(1); fi.play();
        PauseTransition pt = new PauseTransition(Duration.millis(2000));
        pt.setOnFinished(e -> {
            FadeTransition fo = new FadeTransition(Duration.millis(300), lbl);
            fo.setFromValue(1); fo.setToValue(0);
            fo.setOnFinished(ev -> popup.hide()); fo.play();
        });
        pt.play();
    }

    // ── Animation helpers ─────────────────────────────────────────

    public static void animScale(Node n, double target) {
        ScaleTransition st = new ScaleTransition(Duration.millis(140), n);
        st.setToX(target); st.setToY(target); st.play();
    }

    public static FadeTransition fadeIn(Node n, int ms) {
        n.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(ms), n);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
        return ft;
    }

    public static TranslateTransition slideUp(Node n, int ms) {
        n.setTranslateY(30);
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), n);
        tt.setFromY(30); tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.play();
        return tt;
    }
}
