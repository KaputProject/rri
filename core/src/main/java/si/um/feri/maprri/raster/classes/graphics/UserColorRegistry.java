package si.um.feri.maprri.raster.classes.graphics;

import com.badlogic.gdx.graphics.Color;
import java.util.HashMap;
import java.util.Map;

public class UserColorRegistry {
    private static final Map<String, Color> userColors = new HashMap<>();
    private static int colorIndex = 0;

    private static final Color[] PALETTE = {
        new Color(0.2f, 0.6f, 1f, 1f), // Sky blue
        new Color(1f, 0.8f, 0.2f, 1f), // Amber
        new Color(0.8f, 0.4f, 1f, 1f), // Purple
        new Color(0.2f, 0.9f, 0.9f, 1f), // Cyan
        new Color(1f, 0.6f, 0.2f, 1f),// Orange
        new Color(1f, 0.4f, 0.4f, 1f), // Soft red
        new Color(0.4f, 0.9f, 0.4f, 1f), // Light green

    };

    public static Color getColor(String userId) {
        if (userId == null) {
            return new Color(0.5f, 0.5f, 0.8f, 1f);
        }
        return userColors.computeIfAbsent(userId, k -> {
            Color c = PALETTE[colorIndex % PALETTE.length];
            colorIndex++;
            return c;
        });
    }

    public static void registerUser(String userId, Color color) {
        userColors.put(userId, color);
    }

    public static void clear() {
        userColors.clear();
        colorIndex = 0;
    }
}
