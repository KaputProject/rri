package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import si.um.feri.maprri.raster.actors.PieChartActor;
import si.um.feri.maprri.raster.classes.graphics.ColumnMode;
import si.um.feri.maprri.raster.classes.graphics.UserColorRegistry;
import si.um.feri.maprri.raster.manager.DataManager;

import java.util.ArrayList;
import java.util.List;

public class HudView {
    private Stage stage;
    private BitmapFont font;
    private LabelStyle labelStyle;
    private Table detailsTable;
    private Container<Table> detailsContainer;
    private Pixmap pixmap;
    private FreeTypeFontGenerator fontGenerator;
    private Label familyModeLabel;
    private boolean familyMode = true;
    private DataManager dataManager;
    Color inflowColor = new Color(0.3f, 0.8f, 0.3f, 1f); // green
    Color outflowColor = new Color(0.8f, 0.3f, 0.3f, 1f); // red
    List<Color> colors = new ArrayList<>();

    // Filter panel
    private Table filterTable;
    private Container<Table> filterContainer;
    private SelectBox<String> modeSelectBox;
    private SelectBox<String> userSelectBox;
    private SelectBox<String> minAmountSelectBox;
    private FilterChangeListener filterChangeListener;
    private ColumnMode currentMode = ColumnMode.COMBINED;
    private String currentUserId = null;
    private float currentMinAmount = 0f;

    // Callback interface for filter changes
    public interface FilterChangeListener {
        void onModeChanged(ColumnMode mode);
        void onUserChanged(String userId); // null = family view
        void onMinAmountChanged(float minAmount);
    }
    public HudView(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public void create() {
        stage = new Stage(new ScreenViewport());
        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("core/fonts/Roboto/static/Roboto-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 18;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ščžŠČŽ";
        font = fontGenerator.generateFont(parameter);
        labelStyle = new LabelStyle(font, Color.WHITE);
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        pixmap.fill();
        familyModeLabel = new Label("FAMILY MODE: OFF", labelStyle);


        showFamilyLabel();
        showControls();
        showFilterPanel();
    }

    public Stage getStage() {
        return stage;
    }

    public void setFamilyMode(boolean on) {
        familyMode = on;
        if (familyModeLabel != null) {
            familyModeLabel.setText("FAMILY MODE: " + (on ? "ON" : "OFF"));
        }
        // Sync the user select box if it exists
        if (userSelectBox != null) {
            if (on) {
                userSelectBox.setSelectedIndex(0); // Family view
                currentUserId = null;
            }
        }
    }

    public ColumnMode getCurrentMode() {
        return currentMode;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void showFamilyLabel() {
        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        Table topCenterTable = new Table();
        topCenterTable.top();
        topCenterTable.setBackground(grayBg);
        topCenterTable.add(familyModeLabel).padTop(10).center();
        topCenterTable.setFillParent(false);
        topCenterTable.setWidth(Gdx.graphics.getWidth());
        topCenterTable.setHeight(familyModeLabel.getHeight() + 20);
        topCenterTable.setPosition(0, Gdx.graphics.getHeight() - topCenterTable.getHeight());
        stage.addActor(topCenterTable);
    }

    public void showControls() {
        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        Table table = new Table();
        table.top().left();
        table.setBackground(grayBg);

        table.pad(10);

        String[] lines = {
            "Controls:",
            "W / S / A / D  - Move map",
            "Q / E            - Zoom in/out",
            "Arrow keys      - Pitch / Yaw",
            "Mouse drag       - Rotate camera",
            "Tap                 - Select / interact",
            "R        - Debug mode",
            "I    - Inverse mouse controls (x axis)",
            "F   - Show Whole family data",
            "L   - Toggle location labels",
        };

        for (String line : lines) {
            Label label = new Label(line, labelStyle);
            table.add(label).left().pad(2).row();
        }

        Table root = new Table();
        root.setFillParent(true);
        // Move controls down to avoid overlapping with the top banner (approx 40-50px height)
        root.top().left().add(table).padTop(50).padLeft(10);

        stage.addActor(root);
    }

    public void setFilterChangeListener(FilterChangeListener listener) {
        this.filterChangeListener = listener;
    }

    public void showFilterPanel() {
        if (filterContainer != null) {
            filterContainer.remove();
        }

        filterTable = new Table();
        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        filterTable.setBackground(grayBg);
        filterTable.pad(10);

        // Create skin for UI elements
        Skin skin = createBasicSkin();

        // Title
        filterTable.add(new Label("Filters:", labelStyle)).left().padBottom(8).row();

        // Mode filter
        filterTable.add(new Label("Display Mode:", labelStyle)).left().padBottom(2).row();
        modeSelectBox = new SelectBox<>(skin);
        modeSelectBox.setItems("Combined", "Inflow Only", "Outflow Only");
        modeSelectBox.setSelectedIndex(0);
        modeSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int idx = modeSelectBox.getSelectedIndex();
                ColumnMode mode;
                if (idx == 0) mode = ColumnMode.COMBINED;
                else if (idx == 1) mode = ColumnMode.INFLOW;
                else mode = ColumnMode.OUTFLOW;
                currentMode = mode;
                if (filterChangeListener != null) {
                    filterChangeListener.onModeChanged(mode);
                }
            }
        });
        filterTable.add(modeSelectBox).width(150).padBottom(8).row();

        // User filter (for individual view)
        filterTable.add(new Label("View User:", labelStyle)).left().padBottom(2).row();
        userSelectBox = new SelectBox<>(skin);
        updateUserSelectBox();
        userSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int idx = userSelectBox.getSelectedIndex();
                if (idx == 0) {
                    // Family view
                    currentUserId = null;
                } else {
                    // Individual user
                    int userIdx = idx - 1;
                    if (userIdx >= 0 && userIdx < dataManager.family.size()) {
                        currentUserId = dataManager.family.get(userIdx).getId();
                    } else {
                        currentUserId = null;
                    }
                }
                if (filterChangeListener != null) {
                    filterChangeListener.onUserChanged(currentUserId);
                }
            }
        });
        filterTable.add(userSelectBox).width(150).padBottom(8).row();

        // Minimum amount filter
        filterTable.add(new Label("Min Amount:", labelStyle)).left().padBottom(2).row();
        minAmountSelectBox = new SelectBox<>(skin);
        minAmountSelectBox.setItems("All", "> 10", "> 50", "> 100", "> 500", "> 1000");
        minAmountSelectBox.setSelectedIndex(0);
        minAmountSelectBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int idx = minAmountSelectBox.getSelectedIndex();
                float minAmount;
                switch (idx) {
                    case 1: minAmount = 10f; break;
                    case 2: minAmount = 50f; break;
                    case 3: minAmount = 100f; break;
                    case 4: minAmount = 500f; break;
                    case 5: minAmount = 1000f; break;
                    default: minAmount = 0f; break;
                }
                currentMinAmount = minAmount;
                if (filterChangeListener != null) {
                    filterChangeListener.onMinAmountChanged(minAmount);
                }
            }
        });
        filterTable.add(minAmountSelectBox).width(150).row();

        // Position below controls (top-left)
        filterContainer = new Container<>(filterTable);
        filterContainer.top().left().padTop(345).padLeft(10);
        filterContainer.setFillParent(true);

        stage.addActor(filterContainer);
    }

    private void updateUserSelectBox() {
        if (userSelectBox == null) return;

        java.util.List<String> items = new java.util.ArrayList<>();
        items.add("All (Family)");
        for (Person p : dataManager.family) {
            String name = p.getName();
            if (name == null || name.trim().isEmpty()) {
                // Fallback, but do NOT show full id in UI by default.
                String id = p.getId();
                name = (id == null) ? "Unknown" : id.substring(0, Math.min(8, id.length()));
            }
            items.add(name);
        }

        int prevSelected = userSelectBox.getSelectedIndex();
        userSelectBox.setItems(items.toArray(new String[0]));
        // Keep selection if possible
        if (prevSelected >= 0 && prevSelected < items.size()) {
            userSelectBox.setSelectedIndex(prevSelected);
        } else {
            userSelectBox.setSelectedIndex(0);
        }
    }

    private Skin createBasicSkin() {
        Skin skin = new Skin();

        // Create font
        skin.add("default-font", font);

        // Create colors
        Pixmap selectBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        selectBg.setColor(0.3f, 0.3f, 0.3f, 1f);
        selectBg.fill();
        skin.add("select-bg", new Texture(selectBg));
        selectBg.dispose();

        Pixmap listBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        listBg.setColor(0.25f, 0.25f, 0.25f, 1f);
        listBg.fill();
        skin.add("list-bg", new Texture(listBg));
        listBg.dispose();

        Pixmap selectionBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        selectionBg.setColor(0.4f, 0.6f, 0.8f, 1f);
        selectionBg.fill();
        skin.add("selection-bg", new Texture(selectionBg));
        selectionBg.dispose();

        // Label style
        Label.LabelStyle lblStyle = new Label.LabelStyle();
        lblStyle.font = font;
        lblStyle.fontColor = Color.WHITE;
        skin.add("default", lblStyle);

        // List style (use full class name to avoid conflict with java.util.List)
        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle =
            new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle();
        listStyle.font = font;
        listStyle.fontColorSelected = Color.WHITE;
        listStyle.fontColorUnselected = Color.LIGHT_GRAY;
        listStyle.selection = new TextureRegionDrawable(skin.get("selection-bg", Texture.class));
        listStyle.background = new TextureRegionDrawable(skin.get("list-bg", Texture.class));
        skin.add("default", listStyle);

        // ScrollPane style
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollStyle);

        // SelectBox style
        SelectBox.SelectBoxStyle selectStyle = new SelectBox.SelectBoxStyle();
        selectStyle.font = font;
        selectStyle.fontColor = Color.WHITE;
        selectStyle.background = new TextureRegionDrawable(skin.get("select-bg", Texture.class));
        selectStyle.listStyle = listStyle;
        selectStyle.scrollStyle = scrollStyle;
        skin.add("default", selectStyle);

        return skin;
    }


    public void showDetailsForUser(Location location, String userId) {
        System.out.println("Show details for user " + userId + " at location " + location);

        if (detailsContainer != null) {
            detailsContainer.remove();
        }

        detailsTable = new Table();
        detailsTable.top().right();

        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        detailsTable.setBackground(grayBg);
        detailsTable.pad(10);

        // Header
        detailsTable.add(new Label("Details", labelStyle)).center().padBottom(6).row();
        detailsTable.add(new Label("Location: " + location.getIdentifier(), labelStyle)).left().row();
        detailsTable.add(new Label("Address: " + location.getAddress(), labelStyle)).left().padBottom(8).row();

        for (LocationUser user : location.getUsers()) {
            if (user.getId().equals(userId)) {

                Color userColor = UserColorRegistry.getColor(user.getId());

                Label userName = new Label(user.getUsername() + ": ", labelStyle);
                userName.setColor(userColor);
                detailsTable.add(userName).left().padBottom(6).row();

                detailsTable.add(new Label("Transactions: " + user.getNumbOftrans(), labelStyle)).left().row();

                Label inflow = new Label("Inflow: + " + String.format("%.2f", user.getInflow()), labelStyle);
                inflow.setColor(inflowColor);
                detailsTable.add(inflow).left().row();

                Label outflow = new Label("Outflow: - " + String.format("%.2f", user.getOutflow()), labelStyle);
                outflow.setColor(outflowColor);
                detailsTable.add(outflow).left().row();

                List<Float> values = new ArrayList<>();
                values.add((float) user.getInflow());
                values.add((float) user.getOutflow());
                List<Color> colors = new ArrayList<>();
                colors.add(inflowColor);
                colors.add(outflowColor);

                if (user.getInflow() != 0f || user.getOutflow() != 0f) {
                    detailsTable.add(new Label("Inflow vs Outflow:", labelStyle)).left().padTop(8).padBottom(2).row();
                    PieChartActor pieChart = new PieChartActor(values, colors);
                    detailsTable.add(pieChart).center().padBottom(10).row();
                }

                break;
            }
        }

        detailsContainer = new Container<>(detailsTable);
        detailsContainer.top().right().padTop(50).padRight(10); // Top-right corner
        detailsContainer.setFillParent(true);

        stage.addActor(detailsContainer);
    }

    public void showDetailsForFamily(Location location) {
        System.out.println("Show details for family " + location.getIdentifier());

        if (detailsContainer != null) {
            detailsContainer.remove();
        }

        detailsTable = new Table();
        detailsTable.top().right();

        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        detailsTable.setBackground(grayBg);
        detailsTable.pad(10);

        // Header
        detailsTable.add(new Label("Family details", labelStyle)).left().padBottom(6).row();
        detailsTable.add(new Label("Location: " + location.getIdentifier(), labelStyle)).left().row();
        detailsTable.add(new Label("Address: " + location.getAddress(), labelStyle)).left().padBottom(6).row();

        detailsTable.add(new Label("Transactions: " + location.getNumberOfTrans(), labelStyle)).left().row();

        Label totalInflow = new Label("Total inflow: + " + String.format("%.2f", location.getTotal_inflow()), labelStyle);
        totalInflow.setColor(inflowColor);
        detailsTable.add(totalInflow).left().row();

        Label totalOutflow = new Label("Total outflow: - " + String.format("%.2f", location.getTotal_outflow()), labelStyle);
        totalOutflow.setColor(outflowColor);
        detailsTable.add(totalOutflow).left().padBottom(8).row();
        detailsTable.add(new Label("Family Members:", labelStyle)).left().padBottom(4).row();

        for (LocationUser user : location.getUsers()) {
            Color userColor = UserColorRegistry.getColor(user.getId());
            Label userInfo = new Label(String.format("%s: %d trans | +%.2f | -%.2f", user.getUsername(), user.getNumbOftrans(), user.getInflow(), user.getOutflow()), labelStyle);
            userInfo.setColor(userColor);
            detailsTable.add(userInfo).left().padBottom(6).row();
        }
        List<Float> inflows = new ArrayList<>();
        List<Float> outflows = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        for (LocationUser user : location.getUsers()) {
            inflows.add((float) user.getInflow());
            outflows.add((float) user.getOutflow());
            colors.add(UserColorRegistry.getColor(user.getId()));
        }
        if (!inflows.isEmpty() && inflows.stream().anyMatch(f -> f != 0f)) {
            detailsTable.add(new Label("Income (Inflow):", labelStyle)).left().padBottom(2).row();
            PieChartActor inflowPie = new PieChartActor(inflows, colors);
            detailsTable.add(inflowPie).center().padBottom(10).row();
        }
        if (!outflows.isEmpty() && outflows.stream().anyMatch(f -> f != 0f)) {
            detailsTable.add(new Label("Expenses (Outflow):", labelStyle)).left().padBottom(2).row();
            PieChartActor outflowPie = new PieChartActor(outflows, colors);
            detailsTable.add(outflowPie).center().padBottom(10).row();
        }
        detailsContainer = new Container<>(detailsTable);
        detailsContainer.top().right().padTop(50).padRight(10); // Top-right corner
        detailsContainer.setFillParent(true);

        stage.addActor(detailsContainer);
    }

    public void render() {

        if (stage != null) {
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            stage.draw();
        }
    }

    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    public void dispose() {
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
        if (fontGenerator != null) fontGenerator.dispose();
        pixmap.dispose();
    }

    private Label coloredLabel(String text, Color color) {
        Label label = new Label(text, labelStyle);
        label.setColor(color);
        return label;
    }


}
