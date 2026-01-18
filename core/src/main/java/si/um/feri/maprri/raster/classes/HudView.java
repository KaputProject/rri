package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import si.um.feri.maprri.raster.actors.PieChartActor;
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
    }

    public Stage getStage() {
        return stage;
    }

    public void setFamilyMode(boolean on) {
        familyMode = on;
        if (familyModeLabel != null) {
            familyModeLabel.setText("FAMILY MODE: " + (on ? "ON" : "OFF"));
        }
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

        String[] lines = {"Controls:", "W / S / A / D  - Move map", "Q / E            - Zoom in/out", "Arrow keys      - Pitch / Yaw", "Mouse drag       - Rotate camera", "Pinch / Scroll  - Zoom", "Tap                 - Select / interact", "R        - Debug mode", "I    - Inverse mouse controls (x axis)", "F   - Show Whole family data",};

        for (String line : lines) {
            Label label = new Label(line, labelStyle);
            table.add(label).left().pad(2).row();
        }

        Table root = new Table();
        root.setFillParent(true);
        root.top().left().add(table);

        stage.addActor(root);
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
        detailsTable.add(new Label("details", labelStyle)).center().padBottom(6).row();
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
        detailsContainer.top().right().pad(10);
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
        detailsContainer.top().right().pad(10);
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
