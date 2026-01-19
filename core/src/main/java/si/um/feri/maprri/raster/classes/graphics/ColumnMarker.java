package si.um.feri.maprri.raster.classes.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector2;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import si.um.feri.maprri.raster.RasterMap;
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
    public float offsetX = 0;
    public float offsetY = 0;

    public ColumnMarker(ColumnVisual visual, Map map) {
        super(visual.getLat(), visual.getLng(), map);
        this.visual = visual;
        this.map = map;
        this.targetHeight = 0f;

        // Compute pixel position in the same coordinate system as Map tile instances.
        int mapHeight = si.um.feri.maprri.raster.config.Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        this.pixelPosition = MapRasterTiles.getPixelPosition(
            visual.getLat(),
            visual.getLng(),
            MapRasterTiles.TILE_SIZE,
            si.um.feri.maprri.raster.config.Config.ZOOM,
            map.beginTile.x,
            map.beginTile.y,
            mapHeight
        );

        applyColor(RasterMap.isFamilyView());
    }
    public void updateVisualValue(double value) {
        visual.value = value;
    }

    public void setRenderOffset(float dx, float dy) {
        this.offsetX = dx;
        this.offsetY = dy;
    }
    public void applyColor(boolean familyView) {
        Color color;
        if (familyView && visual.userId != null) {
            // Use unique color for each user in family view
            color = UserColorRegistry.getColor(visual.userId);
        } else if (visual.mode != null) {
            // fallback for mode-based coloring
            if (visual.mode == ColumnMode.INFLOW) {
                color = new Color(0.3f, 0.8f, 0.3f, 1f); // green
            } else if (visual.mode == ColumnMode.OUTFLOW) {
                color = new Color(0.8f, 0.3f, 0.3f, 1f); // red
            } else if (visual.mode == ColumnMode.COMBINED) {
                color = visual.value >= 0
                    ? new Color(0.3f, 0.8f, 0.3f, 1f)
                    : new Color(0.8f, 0.3f, 0.3f, 1f);
            } else {
                color = new Color(0.5f, 0.5f, 0.5f, 1f); // fallback
            }
        } else {
            color = new Color(0.5f, 0.5f, 0.5f, 1f);
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
            markerInstance.transform.setToTranslation(
                pixelPosition.x + offsetX,
                pixelPosition.y + offsetY,
                safeHeight / 2f
            );
            markerInstance.transform.scale(1f, 1f, scaleZ);
        }
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
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

        // Make picking a bit more forgiving than the physical 20x20 base.
        float padding = 6f;

        float radius = getHitboxRadius() + padding;
        float size = radius * 2f;
        float half = size / 2f;

        float minX = pos.x + offsetX - half, maxX = pos.x + offsetX + half;
        float minY = pos.y + offsetY - half, maxY = pos.y + offsetY + half;
        float bottomZ = 0f;

        // Use the larger of current/target height so the top is clickable during animation.
        float topZ = Math.max(getCurrentHeight(), getTargetHeight());
        topZ = Math.max(topZ, 1f);

        return new BoundingBox(new Vector3(minX, minY, bottomZ), new Vector3(maxX, maxY, topZ));
    }

}
