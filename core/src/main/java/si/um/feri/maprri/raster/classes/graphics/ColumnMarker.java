package si.um.feri.maprri.raster.classes.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import si.um.feri.maprri.raster.classes.Map;
import si.um.feri.maprri.raster.classes.Marker;

public class ColumnMarker extends Marker {

    private final ColumnVisual visual;
    private float targetHeight;
    private float currentHeight = 0f;
    private boolean visible = true;

    public ColumnMarker(ColumnVisual visual, Map map) {
        super(visual.getLat(), visual.getLng(), map);
        this.visual = visual;
        this.targetHeight = 0f;
        applyColor();
    }

    private void applyColor() {
        Color color;
        if (visual.mode == ColumnMode.COMBINED) {
            color = visual.value >= 0
                ? new Color(0.3f, 0.8f, 0.3f, 1f)
                : new Color(0.8f, 0.3f, 0.3f, 1f);
        } else if (visual.mode == ColumnMode.INFLOW) {
            color = new Color(0.3f, 0.8f, 0.3f, 1f);
        } else {
            color = new Color(0.8f, 0.3f, 0.3f, 1f);
        }

        if (visual.userId != null) {
            color = UserColorRegistry.getColor(visual.userId);
        }

        if (markerInstance != null && !markerInstance.materials.isEmpty()) {
            markerInstance.materials.get(0).set(ColorAttribute.createDiffuse(color));
        }
    }

    public void setTargetHeight(float height) {
        this.targetHeight = height;
    }

    public float getTargetHeight() {
        return targetHeight;
    }

    public void animateTo(float target, float delta) {
        float speed = 5f;
        currentHeight += (target - currentHeight) * speed * delta;

        currentHeight = Math.max(0f, currentHeight);

        updateHeight(currentHeight);
    }

    public void update(float delta) {
        float target = visible ? targetHeight : 0f;
        animateTo(target, delta);
    }

    private void updateHeight(float height) {
        if (markerInstance != null) {
            markerInstance.transform.setToScaling(1f, Math.max(height, 0.01f), 1f);

            float[] pos = getWorldPosition();
            markerInstance.transform.setTranslation(pos[0], height / 2f, pos[1]);
        }
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public boolean isHidden() {
        return !visible && currentHeight < 0.01f;
    }

    public ColumnVisual getVisual() {
        return visual;
    }

    public String getColumnId() {
        return visual.getLocationId() + "_" + visual.userId + "_" + visual.mode;
    }

    public boolean containsPoint(float worldX, float worldZ, float threshold) {
        float[] pos = getWorldPosition();
        return Math.abs(pos[0] - worldX) < threshold &&
            Math.abs(pos[1] - worldZ) < threshold;
    }

    private float[] getWorldPosition() {
        return new float[]{(float) visual.getLng(), (float) visual.getLat()};
    }
}

