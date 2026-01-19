package si.um.feri.maprri.raster.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
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

public class ColumnManager {

    private final DataManager dataManager;
    private final Map map;
    private final java.util.Map<String, ColumnMarker> columns = new HashMap<>();
    private final HeightScaler scaler;
    private ColumnMode currentMode = ColumnMode.COMBINED;
    private String currentUserId = null; // null = family view
    private float minAmount = 0f; // minimum amount filter
    private static final float MARKER_SIZE = 20f;
    private static final float GAP = 0f; // gap inside group (0 = touching)

    // Location labels (billboard text above column groups)
    private final java.util.Map<String, LocationLabel> locationLabels = new HashMap<>();
    private BitmapFont labelFont;
    private SpriteBatch labelBatch;
    private static final float LABEL_HEIGHT_OFFSET = 10f; // Height above tallest column

    public ColumnManager(DataManager dataManager, Map map, float maxHeight) {
        this.dataManager = dataManager;
        this.map = map;
        this.scaler = new HeightScaler(1000, maxHeight);

        // Initialize font for location labels
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("core/fonts/Roboto/static/Roboto-Regular.ttf")
        );
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 2;
        parameter.borderColor = Color.BLACK;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "ščžŠČŽ";
        labelFont = generator.generateFont(parameter);
        generator.dispose();

        labelBatch = new SpriteBatch();
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
            // Apply minimum amount filter
            if (Math.abs(visual.value) < minAmount) {
                continue; // Skip columns below minimum amount
            }

            String key = visual.getLocationId() + "_" + visual.userId + "_" + currentMode;


            ColumnMarker marker = columns.get(key);
            if (marker == null) {
                marker = new ColumnMarker(visual, map);
                columns.put(key, marker);
            }

            float height = scaler.scale(visual.value);

            marker.updateVisualValue(visual.value);
            marker.applyColor(currentUserId == null); // null = family view
            marker.setTargetHeight(height);
            marker.show();
        }
        applyNonOverlappingOffsets();
        updateLocationLabels();
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
        // Group markers by location identifier (not by coordinates!)
        // This ensures each location forms its own group even if coordinates are identical
        java.util.Map<String, java.util.List<ColumnMarker>> locationGroups = new java.util.HashMap<>();

        for (ColumnMarker marker : columns.values()) {
            if (!marker.isVisible()) continue;
            String locationId = marker.getVisual().getLocationId();
            locationGroups.computeIfAbsent(locationId, k -> new java.util.ArrayList<>()).add(marker);
        }

        // First, reset all offsets and assign grid layout within each group
        for (java.util.List<ColumnMarker> group : locationGroups.values()) {
            assignGridOffsets(group);
        }

        // Then separate overlapping location groups
        separateOverlappingGroups(locationGroups);
    }

    /**
     * Updates location labels based on current column positions.
     * One label per location, positioned above the tallest column in the group.
     */
    private void updateLocationLabels() {
        locationLabels.clear();

        // Group visible columns by location
        java.util.Map<String, java.util.List<ColumnMarker>> locationGroups = new java.util.HashMap<>();
        for (ColumnMarker marker : columns.values()) {
            if (!marker.isVisible()) continue;
            String locationId = marker.getVisual().getLocationId();
            locationGroups.computeIfAbsent(locationId, k -> new java.util.ArrayList<>()).add(marker);
        }

        // Create a label for each location group
        for (java.util.Map.Entry<String, java.util.List<ColumnMarker>> entry : locationGroups.entrySet()) {
            java.util.List<ColumnMarker> group = entry.getValue();
            if (group.isEmpty()) continue;

            // Get location name from first marker
            String locationName = group.get(0).getVisual().getLocation().getIdentifier();

            // Calculate center position and max height of the group
            float sumX = 0, sumY = 0;
            float maxHeight = 0;
            for (ColumnMarker m : group) {
                Vector2 pos = m.getPixelPosition();
                sumX += pos.x + m.offsetX;
                sumY += pos.y + m.offsetY;
                maxHeight = Math.max(maxHeight, m.getTargetHeight());
            }
            float centerX = sumX / group.size();
            float centerY = sumY / group.size();
            float labelZ = maxHeight + LABEL_HEIGHT_OFFSET;

            // Create label
            LocationLabel label = new LocationLabel(locationName, centerX, centerY, labelZ, labelFont);
            locationLabels.put(entry.getKey(), label);
        }
    }

    /**
     * Assigns grid offsets to markers in a group.
     * Layout: 2 columns wide, rows grow as needed.
     * Markers touch each other (GAP=0), centered around the location point.
     */
    private void assignGridOffsets(java.util.List<ColumnMarker> group) {
        int n = group.size();
        if (n == 0) return;

        if (n == 1) {
            // Single column - center it
            group.get(0).setRenderOffset(0, 0);
            return;
        }

        // Grid layout: 2 columns wide
        int gridCols = 2;
        int rows = (n + gridCols - 1) / gridCols;

        // Calculate total grid size (actual columns used, not max)
        int actualCols = Math.min(n, gridCols);
        float totalWidth = actualCols * MARKER_SIZE;
        float totalHeight = rows * MARKER_SIZE;

        // Starting position (top-left of grid, offset so grid is centered)
        float startX = -totalWidth / 2f + MARKER_SIZE / 2f;
        float startY = totalHeight / 2f - MARKER_SIZE / 2f;

        for (int i = 0; i < n; i++) {
            int col = i % gridCols;
            int row = i / gridCols;
            float dx = startX + col * MARKER_SIZE;
            float dy = startY - row * MARKER_SIZE;
            group.get(i).setRenderOffset(dx, dy);
        }
    }

    /**
     * Separates groups of columns at different locations that overlap.
     * Groups are pushed apart while trying to stay close to their original position.
     */
    private void separateOverlappingGroups(java.util.Map<String, java.util.List<ColumnMarker>> locationGroups) {
        java.util.List<java.util.List<ColumnMarker>> groupList = new java.util.ArrayList<>(locationGroups.values());
        if (groupList.size() < 2) return;

        // Minimum gap between different location groups
        float minGapBetweenGroups = 3f;
        int maxIterations = 50;

        for (int iter = 0; iter < maxIterations; iter++) {
            boolean anyOverlap = false;

            // Calculate current bounds for all groups
            java.util.List<float[]> allBounds = new java.util.ArrayList<>();
            for (java.util.List<ColumnMarker> group : groupList) {
                allBounds.add(calculateGroupBounds(group));
            }

            // Check each pair of groups for overlap
            for (int i = 0; i < groupList.size(); i++) {
                for (int j = i + 1; j < groupList.size(); j++) {
                    float[] boundsA = allBounds.get(i);
                    float[] boundsB = allBounds.get(j);

                    if (boundsOverlap(boundsA, boundsB, minGapBetweenGroups)) {
                        anyOverlap = true;

                        // Calculate current centers
                        float cxA = (boundsA[0] + boundsA[2]) / 2f;
                        float cyA = (boundsA[1] + boundsA[3]) / 2f;
                        float cxB = (boundsB[0] + boundsB[2]) / 2f;
                        float cyB = (boundsB[1] + boundsB[3]) / 2f;

                        // Direction from A to B
                        float dirX = cxB - cxA;
                        float dirY = cyB - cyA;
                        float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);

                        if (len < 0.001f) {
                            // Groups are at exact same position - use spiral pattern based on index
                            double angle = (i * 137.5) * Math.PI / 180.0; // golden angle for good distribution
                            dirX = (float) Math.cos(angle);
                            dirY = (float) Math.sin(angle);
                            len = 1f;
                        }

                        // Normalize direction
                        dirX /= len;
                        dirY /= len;

                        // Calculate how much they overlap
                        float widthA = boundsA[2] - boundsA[0];
                        float widthB = boundsB[2] - boundsB[0];
                        float heightA = boundsA[3] - boundsA[1];
                        float heightB = boundsB[3] - boundsB[1];

                        float overlapX = (widthA / 2f + widthB / 2f + minGapBetweenGroups) - Math.abs(cxB - cxA);
                        float overlapY = (heightA / 2f + heightB / 2f + minGapBetweenGroups) - Math.abs(cyB - cyA);

                        // Push by the minimum overlap needed to separate
                        float pushAmount = Math.max(1f, Math.min(overlapX, overlapY) / 2f + 0.5f);

                        // Apply push to both groups (each moves half the distance)
                        applyGroupOffset(groupList.get(i), -dirX * pushAmount, -dirY * pushAmount);
                        applyGroupOffset(groupList.get(j), dirX * pushAmount, dirY * pushAmount);
                    }
                }
            }

            if (!anyOverlap) break;
        }
    }

    /**
     * Apply additional offset to all markers in a group.
     */
    private void applyGroupOffset(java.util.List<ColumnMarker> group, float dx, float dy) {
        for (ColumnMarker m : group) {
            m.setRenderOffset(m.offsetX + dx, m.offsetY + dy);
        }
    }

    /**
     * Calculate the bounding box for a group of columns.
     * Returns [minX, minY, maxX, maxY]
     */
    private float[] calculateGroupBounds(java.util.List<ColumnMarker> group) {
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;

        float halfSize = MARKER_SIZE / 2f;

        for (ColumnMarker m : group) {
            Vector2 pos = m.getPixelPosition();
            float x = pos.x + m.offsetX;
            float y = pos.y + m.offsetY;
            minX = Math.min(minX, x - halfSize);
            minY = Math.min(minY, y - halfSize);
            maxX = Math.max(maxX, x + halfSize);
            maxY = Math.max(maxY, y + halfSize);
        }

        return new float[]{minX, minY, maxX, maxY};
    }

    /**
     * Check if two bounding boxes overlap (with margin).
     */
    private boolean boundsOverlap(float[] a, float[] b, float margin) {
        return !(a[2] + margin < b[0] || b[2] + margin < a[0] ||
                 a[3] + margin < b[1] || b[3] + margin < a[1]);
    }


    public void render(ModelBatch batch, Environment environment) {
        for (ColumnMarker marker : columns.values()) {
            marker.render(batch, environment);
        }
    }

    /**
     * Renders location labels as billboard text (always facing camera).
     * Call this after render() and after modelBatch.end().
     */
    public void renderLabels(Camera camera) {
        if (locationLabels.isEmpty()) return;

        labelBatch.begin();
        for (LocationLabel label : locationLabels.values()) {
            label.render(labelBatch, labelFont, camera);
        }
        labelBatch.end();
    }

    public void setMode(ColumnMode mode) {
        this.currentMode = mode;
        rebuild();
    }

    public void setUserId(String userId) {
        this.currentUserId = userId;
        rebuild();
    }

    public void setMinAmount(float minAmount) {
        this.minAmount = minAmount;
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
        locationLabels.clear();
        if (labelFont != null) {
            labelFont.dispose();
        }
        if (labelBatch != null) {
            labelBatch.dispose();
        }
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
        ColumnMarker best = null;
        float bestDst2 = Float.POSITIVE_INFINITY;

        Vector3 tmpIntersection = new Vector3();

        for (ColumnMarker cm : columns.values()) {
            if (cm.intersectsRay(ray, tmpIntersection)) {
                float dst2 = ray.origin.dst2(tmpIntersection);
                if (dst2 < bestDst2) {
                    bestDst2 = dst2;
                    best = cm;
                    if (intersection != null) {
                        intersection.set(tmpIntersection);
                    }
                }
            }
        }
        return best;
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
