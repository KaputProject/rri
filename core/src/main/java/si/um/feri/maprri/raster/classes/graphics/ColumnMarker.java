package si.um.feri.maprri.raster.classes.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import si.um.feri.maprri.raster.classes.Map;
import si.um.feri.maprri.raster.classes.Marker;
import si.um.feri.maprri.raster.utils.MapRasterTiles;

public class ColumnMarker extends Marker {

    private final ColumnVisual visual;
    private final Map map;
    private float targetHeight;
    private float currentHeight = 0f;
    private boolean visible = true;
    private Vector2 pixelPosition;
    private BoundingBox hitbox = new BoundingBox();

    public ColumnMarker(ColumnVisual visual, Map map) {
        super(visual.getLat(), visual.getLng(), map);
        this.visual = visual;
        this.map = map;
        this.targetHeight = 0f;
        // Izračunaj pixel pozicijo iz lat/lng
        this.pixelPosition = MapRasterTiles.getPixelPosition(
            visual.getLat(),
            visual.getLng(),
            map.beginTile.x,
            map.beginTile.y
        );
        applyColor();
    }
    public void updateVisualValue(double value) {
        visual.value = value;
    }

    public void applyColor() {
        Color color;
        if (visual.mode == ColumnMode.COMBINED) {
            System.out.println("Value: " + visual.value + "combined mode");
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
            // Stolpec raste v Z smeri (gor), pozicija je na X-Y ravnini zemljevida
            // Osnovni box iz Marker je 20x20x40, torej skaliramo Z os
            float safeHeight = Math.max(height, 0.01f);
            float scaleZ = safeHeight / 40f; // 40f je osnovna višina box modela

            // Resetiraj transformacijo in nastavi pozicijo ter skaliranje
            markerInstance.transform.idt();
            // Pozicija: stolpec na X,Y, dvignjen za polovico višine da je dno na Z=0
            markerInstance.transform.setToTranslation(pixelPosition.x, pixelPosition.y, safeHeight / 2f);
            markerInstance.transform.scale(1f, 1f, scaleZ);
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

    public boolean containsPoint(float worldX, float worldY, float threshold) {
        return Math.abs(pixelPosition.x - worldX) < threshold &&
            Math.abs(pixelPosition.y - worldY) < threshold;
    }
    public Vector2 getPixelPosition() {
        return pixelPosition;
    }
    public float getCurrentHeight() {
        return currentHeight;
    }
    @Override
    public BoundingBox getHitboxBoundingBox() {
        Vector2 pos = getPixelPosition();
        float radius = getHitboxRadius();
        float size = radius * 2f;
        float half = size / 2f;
        float minX = pos.x - half, maxX = pos.x + half;
        float minY = pos.y - half, maxY = pos.y + half;
        float bottomZ = 0f, topZ = getCurrentHeight();
        return new BoundingBox(new Vector3(minX, minY, bottomZ), new Vector3(maxX, maxY, topZ));
    }

}

