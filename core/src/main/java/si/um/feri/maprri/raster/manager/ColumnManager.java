package si.um.feri.maprri.raster.manager;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.Camera;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import si.um.feri.maprri.raster.RasterMap;
import si.um.feri.maprri.raster.classes.Location;
import si.um.feri.maprri.raster.classes.Map;
import si.um.feri.maprri.raster.classes.graphics.*;

import java.util.*;

import static si.um.feri.maprri.raster.RasterMap.isFamilyView;

public class ColumnManager {

    private final DataManager dataManager;
    private final Map map;
    private final java.util.Map<String, ColumnMarker> columns = new HashMap<>();
    private final HeightScaler scaler;
    private ColumnMode currentMode = ColumnMode.COMBINED;
    private String currentUserId = null; // null = family view
    private static final float MARKER_SIZE = 20f;
    private static final float GAP = 2f; // minimalna razdalja, da se ne dotikajo
    private static final float CELL = MARKER_SIZE + GAP;

    public ColumnManager(DataManager dataManager, Map map, float maxHeight) {
        this.dataManager = dataManager;
        this.map = map;
        this.scaler = new HeightScaler(1000, maxHeight);
    }

    public void rebuild() {
        // Označi vse obstoječe za skrivanje
        for (ColumnMarker marker : columns.values()) {
            marker.hide();
        }

        // Pridobi vizualne podatke
        List<ColumnVisual> visuals = dataManager.getColumnVisuals(currentMode, currentUserId);

        // Posodobi max vrednost za skaliranje
        double maxValue = dataManager.getMaxValue(currentMode, currentUserId);
        scaler.setMaxValue(maxValue);

        // Ustvari ali posodobi stolpce
        for (ColumnVisual visual : visuals) {
            String key = visual.getLocationId() + "_" + visual.userId + "_" + currentMode;


            ColumnMarker marker = columns.get(key);
            if (marker == null) {
                marker = new ColumnMarker(visual, map);
                columns.put(key, marker);
            }

            float height = scaler.scale(visual.value);

            marker.updateVisualValue(visual.value);
            marker.applyColor(isFamilyView());
            marker.setTargetHeight(height);
            marker.show();
        }
        applyNonOverlappingOffsets();
    }

    public void update(float delta) {
        Iterator<java.util.Map.Entry<String, ColumnMarker>> it = columns.entrySet().iterator();
        while (it.hasNext()) {
            ColumnMarker marker = it.next().getValue();
            marker.update(delta);

            // Odstrani popolnoma skrite stolpce
            if (marker.isHidden()) {
                it.remove();
            }
        }
    }
    private void applyNonOverlappingOffsets() {
        java.util.Map<String, java.util.List<ColumnMarker>> positionGroups = new java.util.HashMap<>();
        float threshold = 20.0f;

        // Group markers by quantized position
        for (ColumnMarker marker : columns.values()) {
            Vector2 pos = marker.getPixelPosition();
            String key = Math.round(pos.x / threshold) + "_" + Math.round(pos.y / threshold);
            positionGroups.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(marker);
        }

        // Assign offsets within each group
        for (java.util.List<ColumnMarker> group : positionGroups.values()) {
            int n = group.size();
            if (isFamilyView() && n == 2) {
                // Place side by side as 1x2 rectangle (share a full side)
                float markerWidth = 20f; // Adjust to your actual marker width if needed
                group.get(0).setRenderOffset(-markerWidth / 2f, 0);
                group.get(1).setRenderOffset(markerWidth / 2f, 0);
            } else if (n > 1) {
                float radius = 15f;
                for (int i = 0; i < n; i++) {
                    double angle = 2 * Math.PI * i / n;
                    float dx = (float) (radius * Math.cos(angle));
                    float dy = (float) (radius * Math.sin(angle));
                    group.get(i).setRenderOffset(dx, dy);
                }
            } else {
                group.get(0).setRenderOffset(0, 0);
            }
        }

        // Further separate groups that are still too close
        java.util.List<Vector2> groupCenters = new java.util.ArrayList<>();
        java.util.List<java.util.List<ColumnMarker>> groupList = new java.util.ArrayList<>(positionGroups.values());
        for (java.util.List<ColumnMarker> group : groupList) {
            Vector2 pos = group.get(0).getPixelPosition();
            groupCenters.add(new Vector2(pos.x + group.get(0).offsetX, pos.y + group.get(0).offsetY));
        }
        float minGroupDist = 30f; // Minimum allowed distance between group centers
        for (int i = 0; i < groupCenters.size(); i++) {
            for (int j = i + 1; j < groupCenters.size(); j++) {
                Vector2 a = groupCenters.get(i);
                Vector2 b = groupCenters.get(j);
                if (a.dst(b) < minGroupDist) {
                    // Push groups apart
                    Vector2 dir = new Vector2(b).sub(a).nor();
                    if (dir.isZero()) dir.set(1, 0);
                    dir.scl((minGroupDist - a.dst(b)) / 2f);
                    for (ColumnMarker m : groupList.get(i)) {
                        m.setRenderOffset(m.offsetX - dir.x, m.offsetY - dir.y);
                    }
                    for (ColumnMarker m : groupList.get(j)) {
                        m.setRenderOffset(m.offsetX + dir.x, m.offsetY + dir.y);
                    }
                    // Update group centers
                    a.add(dir.scl(-1));
                    b.add(dir);
                }
            }
        }
    }


    public void render(ModelBatch batch, Environment environment) {
        for (ColumnMarker marker : columns.values()) {
            marker.render(batch, environment);
        }
    }

    public void setMode(ColumnMode mode) {
        this.currentMode = mode;
        rebuild();
    }

    public void setUserId(String userId) {
        this.currentUserId = userId;
        rebuild();
    }

    public void setFamilyView() {
        setUserId(null);
    }

    public ColumnMode getCurrentMode() {
        return currentMode;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public ColumnMarker getColumnAt(float worldX, float worldZ) {
        float threshold = 0.5f;
        for (ColumnMarker marker : columns.values()) {
            if (marker.containsPoint(worldX, worldZ, threshold)) {
                return marker;
            }
        }
        return null;
    }
    public void onDataUpdated() {
        rebuild();
    }
    public void dispose() {
        columns.clear();
    }
    public void renderHitboxes(ShapeRenderer shapeRenderer) {
        for (ColumnMarker cm : columns.values()) {
            BoundingBox box = cm.getHitboxBoundingBox();
            Vector3 min = box.min;
            Vector3 max = box.max;
            // Bottom rectangle
            shapeRenderer.line(min.x, min.y, min.z, max.x, min.y, min.z);
            shapeRenderer.line(max.x, min.y, min.z, max.x, max.y, min.z);
            shapeRenderer.line(max.x, max.y, min.z, min.x, max.y, min.z);
            shapeRenderer.line(min.x, max.y, min.z, min.x, min.y, min.z);
            // Top rectangle
            shapeRenderer.line(min.x, min.y, max.z, max.x, min.y, max.z);
            shapeRenderer.line(max.x, min.y, max.z, max.x, max.y, max.z);
            shapeRenderer.line(max.x, max.y, max.z, min.x, max.y, max.z);
            shapeRenderer.line(min.x, max.y, max.z, min.x, min.y, max.z);
            // Vertical lines
            shapeRenderer.line(min.x, min.y, min.z, min.x, min.y, max.z);
            shapeRenderer.line(max.x, min.y, min.z, max.x, min.y, max.z);
            shapeRenderer.line(max.x, max.y, min.z, max.x, max.y, max.z);
            shapeRenderer.line(min.x, max.y, min.z, min.x, max.y, max.z);
        }
    }
    public ColumnMarker getHitColumn(Ray ray, Vector3 intersection) {
        for (ColumnMarker cm : columns.values()) {
            if (cm.intersectsRay(ray, intersection)) {
                return cm;
            }
        }
        return null;
    }
    public ColumnMarker getMarkerByLoc(Location loc) {
        for (ColumnMarker cm : columns.values()) {
          if(cm.getVisual().getLocation().equals(loc)) {
                return cm;
          }
        }
        return null;

    }
}
