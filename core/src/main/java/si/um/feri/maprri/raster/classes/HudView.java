package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HudView {
    private Stage stage;
    private BitmapFont font;
    private LabelStyle labelStyle;

    public void create() {
        stage = new Stage(new ScreenViewport());
        font = new BitmapFont();
        labelStyle = new LabelStyle(font, Color.WHITE);
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        pixmap.fill();
        TextureRegionDrawable grayBg = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();
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
            "R        - Debug mode"
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

    public Stage getStage() {
        return stage;
    }

    public void render() {

        if (stage != null) {
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1/30f));
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
    }
}
