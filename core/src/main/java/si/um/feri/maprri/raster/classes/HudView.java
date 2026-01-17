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
import org.json.JSONArray;
import org.json.JSONObject;

public class HudView {
    private Stage stage;
    private BitmapFont font;
    private LabelStyle labelStyle;
    private Table detailsTable;
    private Container<Table> detailsContainer;
    private Pixmap pixmap;
    private FreeTypeFontGenerator fontGenerator;


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
        showControls();
    }

    public Stage getStage() {
        return stage;
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

    public void showDetails(JSONObject json) {
        if (detailsContainer != null) {
            detailsContainer.remove();
        }
        detailsTable = new Table();
        detailsTable.top().right();
        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        detailsTable.setBackground(grayBg);
        detailsTable.pad(10);

        // Format and add data
        detailsTable.add(new Label("Details:", labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Name: " + json.optString("identifier"), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Address: " + json.optString("address"), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Transactions: " + json.optInt("number_of_transactions"), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Inflow: " + String.format("%.2f", json.optDouble("total_inflow")), labelStyle)).left().pad(2).row();
        detailsTable.add(new Label("Outflow: " + String.format("%.2f", json.optDouble("total_outflow")), labelStyle)).left().pad(2).row();

        JSONArray users = json.optJSONArray("users");
        if (users != null && users.length() > 0) {
            detailsTable.add(new Label("Users:", labelStyle)).left().pad(2).row();
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String userInfo = String.format(
                    "%s: %d trans, inflow %.2f, outflow %.2f",
                    user.optString("username"),
                    user.optInt("numbOftrans"),
                    user.optDouble("inflow"),
                    user.optDouble("outflow")
                );
                detailsTable.add(new Label(userInfo, labelStyle)).left().pad(2).row();
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
