package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;

import java.io.IOException;
import java.util.HashMap;

import si.um.feri.maprri.raster.config.Config;
import si.um.feri.maprri.raster.utils.Geolocation;
import si.um.feri.maprri.raster.utils.MapRasterTiles;
import si.um.feri.maprri.raster.utils.ZoomXY;

public class Map {
    public java.util.Map<ZoomXY, Tile> loadedTiles = new HashMap<>();
    public java.util.Map<ZoomXY, ModelInstance> tileInstances = new HashMap<>();

    public ZoomXY beginTile;
    private ZoomXY currentBeginTile;
    private ZoomXY currentCenterTile;

    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);
    private Geolocation currentLocation;

    public Map() {
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Config.ZOOM);
            beginTile = new ZoomXY(Config.ZOOM, centerTile.x - ((Config.NUM_TILES - 1) / 2), centerTile.y - ((Config.NUM_TILES - 1) / 2));
            currentCenterTile = new ZoomXY(centerTile.zoom, centerTile.x, centerTile.y);

            loadTilesAndBuildInstances(centerTile, beginTile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(Vector3 cameraPosition) {
        updateLocation(cameraPosition);
        updateLoadedTilesIfNeeded();
    }

    private void updateLocation(Vector3 cameraPosition) {
        int mapHeight = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        int mapPixelX = (int) cameraPosition.x;
        int mapPixelY = mapHeight - (int) cameraPosition.y;

        int tileX = beginTile.x + (mapPixelX / MapRasterTiles.TILE_SIZE);
        int tileY = beginTile.y + (mapPixelY / MapRasterTiles.TILE_SIZE);

        currentLocation = MapRasterTiles.getGeolocationFromPixel(
            tileX, tileY,
            mapPixelX % MapRasterTiles.TILE_SIZE,
            mapPixelY % MapRasterTiles.TILE_SIZE,
            Config.ZOOM
        );
    }

    private void updateLoadedTilesIfNeeded() {
        ZoomXY centerTile = MapRasterTiles.getTileNumber(currentLocation.lat, currentLocation.lng, Config.ZOOM);
        if (centerTile.x == currentCenterTile.x && centerTile.y == currentCenterTile.y) {
            return;
        }

        try {
            currentBeginTile = new ZoomXY(Config.ZOOM, centerTile.x - ((Config.NUM_TILES - 1) / 2), centerTile.y - ((Config.NUM_TILES - 1) / 2));
            currentCenterTile = new ZoomXY(centerTile.zoom, centerTile.x, centerTile.y);
            loadTilesAndBuildInstances(centerTile, currentBeginTile);

//            int mapWidth = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
//            int mapHeight = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
//            cameraPosition.set(mapWidth / 2f, mapHeight / 2f, cameraPosition.z);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTilesAndBuildInstances(ZoomXY centerTile, ZoomXY currentBeginTile) throws IOException {
        System.out.println("Loading tiles for center tile: " + centerTile.toString());
        ModelBuilder modelBuilder = new ModelBuilder();
        int size = Config.NUM_TILES;
        int tileSize = MapRasterTiles.TILE_SIZE;

//        tileInstances.keySet().removeIf(key ->
//            key.zoom != centerTile.zoom || key.x < currentBeginTile.x || key.x >= currentBeginTile.x + size || key.y < currentBeginTile.y || key.y >= currentBeginTile.y + size
//        );

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int tileX = currentBeginTile.x + x;
                int tileY = currentBeginTile.y + y;
                ZoomXY key = new ZoomXY(centerTile.zoom, tileX, tileY);

                if (!tileInstances.containsKey(key)) {
                    Texture tileTexture = MapRasterTiles.getRasterTile(centerTile.zoom, tileX, tileY);
                    Tile tile = new Tile(tileX, tileY, centerTile.zoom, tileTexture);

                    loadedTiles.put(key, tile);

                    float modelX = (tileX - beginTile.x) * tileSize;
                    float modelY = (beginTile.y - currentBeginTile.y + (size - 1 - y)) * tileSize;

                    Model tileModel = modelBuilder.createRect(
                        modelX, modelY, 0,
                        modelX + tileSize, modelY, 0,
                        modelX + tileSize, modelY + tileSize, 0,
                        modelX, modelY + tileSize, 0,
                        0, 0, 1,
                        new Material(
                            ColorAttribute.createDiffuse(Color.WHITE),
                            TextureAttribute.createDiffuse(tileTexture)
                        ),
                        VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
                    );
                    ModelInstance instance = new ModelInstance(tileModel);
                    tileInstances.put(key, instance);
                }
            }
        }
    }

    private void disposeTilesAndInstances() {
        for (Tile tile : loadedTiles.values()) {
            tile.dispose();
        }
        for (ModelInstance instance : tileInstances.values()) {
            instance.model.dispose();
        }
        loadedTiles.clear();
        tileInstances.clear();
    }

    public void dispose() {
        disposeTilesAndInstances();
    }
}
