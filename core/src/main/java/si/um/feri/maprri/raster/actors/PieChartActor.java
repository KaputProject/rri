package si.um.feri.maprri.raster.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.g2d.Batch;
import java.util.List;

public class PieChartActor extends Actor {
    private List<Float> values;
    private List<Color> colors;
    private float total;

    public PieChartActor(List<Float> values, List<Color> colors) {
        this.values = values;
        this.colors = colors;
        this.total = 0f;
        for (float v : values) total += v;
        setSize(100, 100); // Set pie chart size
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        ShapeRenderer sr = new ShapeRenderer();
        sr.setProjectionMatrix(getStage().getCamera().combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);

        float startAngle = 0f;
        for (int i = 0; i < values.size(); i++) {
            float angle = (values.get(i) / total) * 360f;
            sr.setColor(colors.get(i));
            sr.arc(getX() + getWidth() / 2, getY() + getHeight() / 2, getWidth() / 2, startAngle, angle);
            startAngle += angle;
        }
        sr.end();
        batch.begin();
    }
}
