package si.um.feri.maprri.raster.manager;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.Camera;

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
            marker.setTargetHeight(height);
            marker.show();
        }
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
}
