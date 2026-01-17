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
import si.um.feri.maprri.raster.manager.DataManager;

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
        familyModeLabel = new Label("FAMILY MODE: ON", labelStyle);
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

        String[] lines = {
            "Controls:",
            "W / S / A / D  - Move map",
            "Q / E            - Zoom in/out",
            "Arrow keys      - Pitch / Yaw",
            "Mouse drag       - Rotate camera",
            "Pinch / Scroll  - Zoom",
            "Tap                 - Select / interact",
            "R        - Debug mode",
            "I    - Inverse mouse controls (x axis)",
            "F   - Show Whole family data",
        };

        for (String line : lines) {
            Label label = new Label(line, labelStyle);
            table.add(label).left().pad(2).row();
        }

        Table root = new Table();
        root.setFillParent(true);
        root.top().left().add(table);

        stage.addActor(root);
    }


    public void showDetails(Location location) {
        if (detailsContainer != null) {
            detailsContainer.remove();
        }
        detailsTable = new Table();
        detailsTable.top().right();
        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        detailsTable.setBackground(grayBg);
        detailsTable.pad(10);

        detailsTable.add(new Label("Details:", labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Name: " + location.getIdentifier(), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Address: " + location.getAddress(), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Transactions: " + location.getNumberOfTrans(), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Inflow: " + String.format("%.2f", location.getTotal_inflow()), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Outflow: " + String.format("%.2f", location.getTotal_outflow()), labelStyle)).left().pad(2).row();

        detailsTable.add(new Label("Users:", labelStyle)).left().pad(2).row();
        if (familyMode) {
            for (LocationUser user : location.getUsers()) {
                String userInfo = String.format(
                        "%s: %d trans, inflow %.2f, outflow %.2f",
                        user.getUsername(),
                        user.getNumbOftrans(),
                        user.getInflow(),
                        user.getOutflow()
                );
                detailsTable.add(new Label(userInfo, labelStyle)).left().pad(2).row();
            }
        } else {
            String mainUserId = !dataManager.mainUser.isEmpty() ? dataManager.mainUser.get(0).getId() : null;
            for (LocationUser user : location.getUsers()) {
                if (user.getId().equals(mainUserId)) {
                    String userInfo = String.format(
                            "%s: %d trans, inflow %.2f, outflow %.2f",
                            user.getUsername(),
                            user.getNumbOftrans(),
                            user.getInflow(),
                            user.getOutflow()
                    );
                    detailsTable.add(new Label(userInfo, labelStyle)).left().pad(2).row();
                }
            }
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
}
