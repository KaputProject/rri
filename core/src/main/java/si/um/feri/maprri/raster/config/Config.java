package si.um.feri.maprri.raster.config;

import com.badlogic.gdx.Gdx;

import si.um.feri.maprri.raster.utils.MapRasterTiles;

public class Config {
    public static final int NUM_TILES = 3;
    public static final int ZOOM = 15;
    public static final float INITIAL_ZOOM = 1f;
    public static final int MAP_WIDTH = MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int MAP_HEIGHT = si.um.feri.maprri.raster.utils.MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int HUD_WIDTH = Gdx.graphics.getWidth();
    public static final int HUD_HEIGHT = Gdx.graphics.getHeight();

    public static final float PERSPECTIVE_CAMERA_INITIAL_Z = 300f;
    public static final float PERSPECTIVE_CAMERA_NEAR = 1f;
    public static final float PERSPECTIVE_CAMERA_FAR = 1500f;
    public static final float CAMERA_MOVEMENT_SPEED = 3f;
}
