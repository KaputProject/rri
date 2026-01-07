package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;

import si.um.feri.maprri.raster.utils.MapRasterTiles;

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

    private static String getTilePath(int x, int y, int zoom) {
        return "tiles/" + zoom + "/" + x + "/" + y + ".png";
    }

    public void save() {
        String path = getTilePath(x, y, zoom);
        Pixmap pixmap = texture.getTextureData().consumePixmap();
        PixmapIO.writePNG(Gdx.files.local(path), pixmap);
        pixmap.dispose();
    }

    public static Tile load(int x, int y, int zoom) {
        String path = getTilePath(x, y, zoom);

        if (Gdx.files.local(path).exists()) {
            Texture texture = new Texture(Gdx.files.local(path));
            System.out.println("Tile loaded locally: " + path);

            return new Tile(x, y, zoom, texture);
        } else {
            try {
                Tile tile = new Tile(x, y, zoom, MapRasterTiles.getRasterTile(zoom, x, y));
                tile.save();

                System.out.println("Tile downloaded and saved: " + path);
                return tile;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
