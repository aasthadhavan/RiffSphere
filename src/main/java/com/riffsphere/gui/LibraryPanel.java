package com.riffsphere.gui;

import com.riffsphere.models.*;
import com.riffsphere.modules.*;

import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.*;

import static com.riffsphere.gui.UIFactory.*;

public class LibraryPanel extends BasePanel {

    public static class SongRow {
        private final String id, title, artist, genre, mood, rating, yourRating;
        private final int bpm;
        public SongRow(String id, String title, String artist, String genre, String mood, int bpm, String rating, String yourRating) {
            this.id=id; this.title=title; this.artist=artist; this.genre=genre;
            this.mood=mood; this.bpm=bpm; this.rating=rating; this.yourRating=yourRating;
        }
        public String getId()         { return id; }
        public String getTitle()      { return title; }
        public String getArtist()     { return artist; }
        public String getGenre()      { return genre; }
        public String getMood()       { return mood; }
        public int    getBpm()        { return bpm; }
        public String getRating()     { return rating; }
        public String getYourRating() { return yourRating; }
    }

    private TableView<SongRow>         tbl;
    private ObservableList<SongRow>    libModel;
    private TextField                  searchField;
    private Slider                     bpmMin, bpmMax;
    private ComboBox<String>           genreBox;
    private Slider                     ratingSlider;
    private Label                      bpmLabel, ratingLabel;

    public LibraryPanel(CommandHistory h) { super(h); }
    @Override public String getPanelId()  { return "LIBRARY"; }

    @Override
    protected void buildUI() {
        setTop(topBar("🎵 Your Library"));

        BorderPane body = new BorderPane();
        body.setStyle("-fx-background-color:transparent;");
        body.setTop(buildFilterPanel());
        body.setCenter(buildTable());
        setCenter(body);
    }

    @Override
    public void refresh() {
        super.refresh();
        layout.setStyle("-fx-background-color:" + hex(BG2) + ";");
        applyFilters();
    }

    private VBox buildFilterPanel() {
        VBox fp = new VBox(10);
        fp.setStyle("-fx-background-color:transparent;");
        fp.setPadding(new Insets(14, 26, 10, 26));

        searchField = searchField("Search songs, artists, genres...");
        Button sbtn = roseBtn("Search");
        sbtn.setMaxWidth(100);
        HBox srow = new HBox(10, searchField, sbtn);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        sbtn.setOnAction(e -> applyFilters());
        searchField.setOnAction(e -> applyFilters());

        HBox moodChips = new HBox(8); moodChips.setAlignment(Pos.CENTER_LEFT);
        String[] mf = {"All","happy","sad","angry","relaxed","energetic","focus"};
        Color[]  mc = {GRAY2, C_LIME, C_BLUE, C_RED, C_TEAL, C_ORANGE, C_PURPLE};
        for (int i = 0; i < mf.length; i++) {
            final String mv = mf[i];
            Button cb = chipBtn(cap(mv), mc[i]);
            cb.setOnAction(e -> applyFilters(mv.equals("All") ? null : mv));
            moodChips.getChildren().add(cb);
        }

        bpmMin = makeSlider(40, 200, 40);
        bpmMax = makeSlider(40, 200, 200);
        bpmLabel = new Label("BPM: 40–200");
        bpmLabel.setStyle("-fx-text-fill:" + hex(GRAY2) + ";-fx-font-size:11;");
        bpmMin.valueProperty().addListener((obs,o,n) -> { if(n.doubleValue()>bpmMax.getValue()) bpmMax.setValue(n.doubleValue()); updateBpmLabel(); });
        bpmMax.valueProperty().addListener((obs,o,n) -> updateBpmLabel());

        Set<String> genres = db.allGenres();
        genreBox = new ComboBox<>();
        genreBox.getItems().add("All Genres");
        genreBox.getItems().addAll(genres);
        genreBox.getSelectionModel().selectFirst();
        genreBox.setOnAction(e -> applyFilters());

        ratingSlider = makeSlider(0, 5, 0);
        ratingLabel  = new Label("Min Rating: ★ 0.0");
        ratingLabel.setStyle("-fx-text-fill:" + hex(GRAY2) + ";-fx-font-size:11;");
        ratingSlider.valueProperty().addListener((obs,o,n) -> { ratingLabel.setText("Min Rating: ★ " + n.intValue() + ".0"); applyFilters(); });

        HBox sliderRow = new HBox(12); sliderRow.setAlignment(Pos.CENTER_LEFT);
        Label minL = new Label("Min:"); minL.setStyle("-fx-text-fill:" + hex(GRAY2) + ";-fx-font-size:11;");
        Label maxL = new Label("Max:"); maxL.setStyle("-fx-text-fill:" + hex(GRAY2) + ";-fx-font-size:11;");
        sliderRow.getChildren().addAll(bpmLabel, minL, bpmMin, maxL, bpmMax, hsp(10), genreBox, hsp(10), ratingLabel, ratingSlider);

        fp.getChildren().addAll(srow, moodChips, sliderRow);
        return fp;
    }

    private void updateBpmLabel() {
        bpmLabel.setText("BPM: " + (int)bpmMin.getValue() + "–" + (int)bpmMax.getValue());
        applyFilters();
    }

    private Region buildTable() {
        if (libModel == null) libModel = FXCollections.observableArrayList();
        String bgHex   = hex(BG2);
        String fgHex   = hex(FG);
        String g1Hex   = hex(GRAY1);
        String g2Hex   = hex(GRAY2);
        String goldHex = hex(GOLD);

        tbl = new TableView<>();
        tbl.setStyle(
            "-fx-background-color:" + bgHex + ";" +
            "-fx-control-inner-background:" + bgHex + ";" +
            "-fx-table-cell-border-color:transparent;" +
            "-fx-table-header-border-color:transparent;"
        );
        tbl.setItems(libModel);

        String[] cols  = {"#","Title","Artist","Genre","Mood","BPM","Rating","★ You"};
        String[] props = {"id","title","artist","genre","mood","bpm","rating","yourRating"};

        for (int i = 0; i < cols.length; i++) {
            TableColumn<SongRow, Object> col = new TableColumn<>(cols[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(props[i]));
            final int ci = i;
            col.setCellFactory(column -> new TableCell<SongRow, Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); return; }
                    setText(item.toString());
                    String color = fgHex;
                    if (ci == 0) color = g2Hex;
                    if (ci == 4) color = hex(ACCENT);
                    if (ci == 7) color = goldHex;
                    String rowBg = isSelected() ? hex(ACCENT) + "28" : (getIndex() % 2 == 0 ? bgHex : hex(BG3));
                    setStyle("-fx-text-fill:" + color + ";-fx-background-color:" + rowBg + ";-fx-padding:4 16;-fx-font-size:13px;");
                }
            });
            tbl.getColumns().add(col);
        }

        tbl.setOnMouseClicked(e -> {
            SongRow row = tbl.getSelectionModel().getSelectedItem();
            if (row == null) return;
            
            if (e.getButton().equals(MouseButton.PRIMARY) && e.getClickCount() == 2) {
                db.findSong(row.getId()).ifPresent(this::playSong);
            } else if (e.getButton().equals(MouseButton.SECONDARY)) {
                db.findSong(row.getId()).ifPresent(s -> showPlaylistContextMenu(tbl, s, e.getScreenX(), e.getScreenY()));
            }
        });
        applyFilters();
        return tbl;
    }

    private void applyFilters()                    { applyFilters(null); }
    private void applyFilters(String moodOverride) {
        if (searchField == null) return;
        if (libModel == null) libModel = FXCollections.observableArrayList();
        PlaylistFilter filter = PlaylistFilter.all();
        if (moodOverride != null) filter = filter.and(new PlaylistFilter.MoodFilter(moodOverride));
        String kw = searchField.getText().trim();
        if (!kw.isBlank() && !kw.startsWith("Search")) filter = filter.and(new PlaylistFilter.KeywordFilter(kw));
        int lo = (int)bpmMin.getValue(), hi = (int)bpmMax.getValue();
        if (lo > 40 || hi < 200) filter = filter.and(new PlaylistFilter.BpmFilter(lo, hi));
        String genre = genreBox.getValue();
        if (genre != null && !genre.equals("All Genres")) filter = filter.and(new PlaylistFilter.GenreFilter(genre));
        int minRating = (int)ratingSlider.getValue();
        if (minRating > 0) filter = filter.and(new PlaylistFilter.RatingFilter(minRating));

        List<Song> songs = db.searchAdvanced(filter);
        User u = currentUser();
        libModel.clear();
        for (Song s : songs) {
            double userRating = u != null ? u.getSongRating(s) : s.getRating();
            libModel.add(new SongRow(s.getId(), s.getTitle(), s.getArtist(), s.getGenre(), s.getMood(), s.getBpm(),
                String.format("%.1f ★", s.getRating()), String.format("%.1f ★", userRating)));
        }
    }

    private Slider makeSlider(double min, double max, double val) {
        Slider s = new Slider(min, max, val); s.setPrefWidth(100); return s;
    }
}
