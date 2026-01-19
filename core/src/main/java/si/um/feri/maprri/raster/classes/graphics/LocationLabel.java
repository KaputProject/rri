package si.um.feri.maprri.raster.classes.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import si.um.feri.maprri.raster.RasterMap;

/**
 * A billboard text label that always faces the camera.
 * Used to display location names above column groups.
 */
public class LocationLabel {
    private final String text;
    private final Vector3 worldPosition;
    private final GlyphLayout layout;

    public LocationLabel(String fullName, float worldX, float worldY, float worldZ, BitmapFont font) {
        // Extract first two words (each on its own line)
        this.text = extractTwoWordsMultiline(fullName);
        this.worldPosition = new Vector3(worldX, worldY, worldZ);
        this.layout = new GlyphLayout(font, text);
    }

    private String extractTwoWordsMultiline(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }

        String trimmed = fullName.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length == 0) return "";
        if (parts.length == 1) return parts[0];

        // Two lines
        return parts[0] + "\n" + parts[1];
    }

    /**
     * Renders the label as a billboard (always facing camera).
     * @param batch SpriteBatch to render with
     * @param font BitmapFont to use
     * @param camera The camera to billboard towards
     */
    public void render(SpriteBatch batch, BitmapFont font, Camera camera) {
        if(!RasterMap.isShowMarkerLabels()){
            return;
        }
        // Project world position to screen coordinates
        Vector3 screenPos = camera.project(new Vector3(worldPosition));

        // Only render if in front of camera (z > 0 means visible)
        if (screenPos.z > 0 && screenPos.z < 1) {
            // Center the text horizontally
            float textX = screenPos.x - layout.width / 2f;
            float textY = screenPos.y + layout.height / 2f;

            // Draw text
            font.setColor(Color.WHITE);
            font.draw(batch, text, textX, textY);
        }
    }

    public void updatePosition(float worldX, float worldY, float worldZ) {
        worldPosition.set(worldX, worldY, worldZ);
    }

    public Vector3 getWorldPosition() {
        return worldPosition;
    }

    public String getText() {
        return text;
    }
}
