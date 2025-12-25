package si.um.feri.maprri.raster.config;

import com.badlogic.gdx.Gdx;

import si.um.feri.maprri.raster.utils.MapRasterTiles;

public class Config {
    public static final int NUM_TILES = 3;
    public static final int ZOOM = 15;
    public static final float INITIAL_ZOOM = 1f;
    public static final int MAP_WIDTH = MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int MAP_HEIGHT = MapRasterTiles.TILE_SIZE * NUM_TILES;
    public static final int HUD_WIDTH = Gdx.graphics.getWidth();
    public static final int HUD_HEIGHT = Gdx.graphics.getHeight();

    public static final float CAMERA_MOUSE_PITCH_SPEED = 0.1f;
    public static final float MIN_PITCH = -89f;
    public static final float MAX_PITCH = -15f;
    public static final float INITIAL_PITCH = -89f;

    public static final float CAMERA_SPEED = 800f;
    public static final float CAMERA_Z_SPEED = 250f;
    public static final float CAMERA_PITCH_SPEED = 30f;

    public static final float CAMERA_Z_INITIAL = 800f;
    public static final float CAMERA_MOUSE_YAW = 0.1f;
    public static final float CAMERA_Z_MIN = 100f;
    public static final float CAMERA_Z_MAX = 3000f;
}
