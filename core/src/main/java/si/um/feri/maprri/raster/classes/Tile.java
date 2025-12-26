package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.graphics.Texture;

public class Tile {
    public int x, y, zoom;
    public Texture texture;

    public Tile(int x, int y, int zoom, Texture texture) {
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        this.texture = texture;
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }
}
