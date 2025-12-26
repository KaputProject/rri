package si.um.feri.maprri.raster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import si.um.feri.maprri.raster.classes.CustomPerspectiveCamera;
import si.um.feri.maprri.raster.classes.Tile;
import si.um.feri.maprri.raster.config.Config;
import si.um.feri.maprri.raster.utils.Geolocation;
import si.um.feri.maprri.raster.utils.MapRasterTiles;
import si.um.feri.maprri.raster.utils.ZoomXY;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RasterMap extends ApplicationAdapter implements GestureDetector.GestureListener {
    private ModelBatch modelBatch;
    private CustomPerspectiveCamera perspectiveCamera;
    private ModelInstance markerInstance;
    private Model markerModel;
    private Environment environment;

    private Map<ZoomXY, Tile> loadedTiles = new HashMap<>();
    private Map<ZoomXY, ModelInstance> tileInstances = new HashMap<>();

    private ZoomXY beginTile;
    private ZoomXY currentBeginTile;
    private ZoomXY currentCenterTile;

    private Vector3 cameraPosition = new Vector3();
    private float cameraPitch = Config.INITIAL_PITCH;
    private float cameraYaw = 0f;
    private float cameraDistance = Config.CAMERA_Z_INITIAL;

    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);
    private Geolocation currentLocation;

    @Override
    public void create() {
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Config.ZOOM);
            beginTile = new ZoomXY(Config.ZOOM, centerTile.x - ((Config.NUM_TILES - 1) / 2), centerTile.y - ((Config.NUM_TILES - 1) / 2));
            currentCenterTile = new ZoomXY(centerTile.zoom, centerTile.x, centerTile.y);

            loadTilesAndBuildInstances(centerTile, beginTile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.65f, 0.65f, 0.65f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.3f));

        // Test model display
        ModelBuilder modelBuilder = new ModelBuilder();
        markerModel = modelBuilder.createBox(20f, 20f, 40f, new Material(ColorAttribute.createDiffuse(Color.RED)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        markerInstance = new ModelInstance(markerModel);
        Vector2 markerPos2D = MapRasterTiles.getPixelPosition(MARKER_GEOLOCATION.lat, MARKER_GEOLOCATION.lng, beginTile.x, beginTile.y);
        markerInstance.transform.setTranslation(markerPos2D.x, markerPos2D.y, 0f);

        // Here I make our custom perspective camera that handles movement
        perspectiveCamera = new CustomPerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        int mapWidth = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        int mapHeight = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        cameraPosition.set(mapWidth / 2f, mapHeight / 2f, cameraDistance);

        updateCamera();

        modelBatch = new ModelBatch();

        // This is here so that the input works
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput(deltaTime);
        update(deltaTime);
        draw();
    }

    private void update(float delta) {
        updateCamera();
        updateLocation();
        updateLoadedTilesIfNeeded();
    }

    private void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);

        for (ModelInstance tileInstance : tileInstances.values()) {
            modelBatch.render(tileInstance, environment);
        }

        modelBatch.render(markerInstance, environment);
        modelBatch.end();
    }

    private void updateCamera() {
        perspectiveCamera.near = 10f;
        perspectiveCamera.far = 8000f;
        perspectiveCamera.setPositionAndDirection(cameraPosition.x, cameraPosition.y, cameraPosition.z, cameraYaw, cameraPitch);
        perspectiveCamera.update();
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

    private void updateLocation() {
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

    @Override
    public void dispose() {
        modelBatch.dispose();
        markerModel.dispose();
        disposeTilesAndInstances();
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        cameraPitch -= deltaY * Config.CAMERA_MOUSE_PITCH_SPEED;
        cameraPitch = MathUtils.clamp(cameraPitch, Config.MIN_PITCH, Config.MAX_PITCH);
        cameraYaw -= deltaX * Config.CAMERA_MOUSE_YAW;
        cameraYaw = (cameraYaw + 360f) % 360f;
        return true;
    }

    @Override public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }
    @Override public boolean tap(float x, float y, int count, int button) {
        return false;
    }
    @Override public boolean longPress(float x, float y) {
        return false;
    }
    @Override public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }
    @Override public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }
    @Override public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }
    @Override public void pinchStop() {}

    @Override
    public boolean zoom(float initialDistance, float distance) {
        cameraPosition.z *= (initialDistance / distance);
        cameraPosition.z = MathUtils.clamp(cameraPosition.z, 400, 3000);
        return true;
    }

    private void handleInput(float delta) {
        int mapWidth = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        int mapHeight = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        float moveSpeed = Config.CAMERA_SPEED * delta * (cameraPosition.z / Config.CAMERA_Z_INITIAL);

        float moveF = 0, moveR = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveF += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveF -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveR += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveR -= 1;

        // Here we have to calculate how much to move in X and Y depending on camera yaw
        float yaw = MathUtils.degreesToRadians * cameraYaw;
        float sinYaw = (float) Math.sin(yaw), cosYaw = (float) Math.cos(yaw);

        cameraPosition.x += (sinYaw * moveF + cosYaw * moveR) * moveSpeed;
        cameraPosition.y += (cosYaw * moveF - sinYaw * moveR) * moveSpeed;
//        cameraPosition.x = MathUtils.clamp(cameraPosition.x, 0, mapWidth);
//        cameraPosition.y = MathUtils.clamp(cameraPosition.y, 0, mapHeight);

        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            cameraPosition.z += Config.CAMERA_Z_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            cameraPosition.z -= Config.CAMERA_Z_SPEED * delta;
        }
        cameraPosition.z = MathUtils.clamp(cameraPosition.z, Config.CAMERA_Z_MIN, Config.CAMERA_Z_MAX);

        // This determines how much the camera pitches up and down
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cameraPitch += Config.CAMERA_PITCH_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cameraPitch -= Config.CAMERA_PITCH_SPEED * delta;
        }
        cameraPitch = MathUtils.clamp(cameraPitch, Config.MIN_PITCH, Config.MAX_PITCH);

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cameraYaw -= Config.CAMERA_PITCH_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraYaw += Config.CAMERA_PITCH_SPEED * delta;
        }
        cameraYaw = (cameraYaw + 360f) % 360f;
    }
}
