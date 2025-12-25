package si.um.feri.maprri.raster;

import static si.um.feri.maprri.raster.utils.MapRasterTiles.mergeMapTiles;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
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
import si.um.feri.maprri.raster.config.Config;
import si.um.feri.maprri.raster.utils.Geolocation;
import si.um.feri.maprri.raster.utils.MapRasterTiles;
import si.um.feri.maprri.raster.utils.ZoomXY;

import java.io.IOException;

public class RasterMap extends ApplicationAdapter implements GestureDetector.GestureListener {
    private ModelBatch modelBatch;
    private CustomPerspectiveCamera perspectiveCamera;
    private ModelInstance markerInstance;
    private Model markerModel;
    private Environment environment;

    private Texture[] mapTiles;
    private ZoomXY beginTile;
    private Model mapModel;
    private ModelInstance mapInstance;

    private Vector3 cameraPosition = new Vector3();
    private float cameraPitch = Config.INITIAL_PITCH;
    private float cameraYaw = 0f;
    private float cameraDistance = Config.CAMERA_Z_INITIAL;

    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);

    @Override
    public void create() {
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Config.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Config.NUM_TILES);
            beginTile = new ZoomXY(Config.ZOOM, centerTile.x - ((Config.NUM_TILES - 1) / 2), centerTile.y - ((Config.NUM_TILES - 1) / 2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int mapWidth = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        int mapHeight = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        Texture mapTexture = mergeMapTiles(mapTiles, Config.NUM_TILES, MapRasterTiles.TILE_SIZE);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.65f, 0.65f, 0.65f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.3f));

        // Here i create the map model as a single large plane with the merged texture
        modelBatch = new ModelBatch();
        ModelBuilder modelBuilder = new ModelBuilder();
        mapModel = modelBuilder.createRect(
            0, 0, 0,
            mapWidth, 0, 0,
            mapWidth, mapHeight, 0,
            0, mapHeight, 0,
            0, 0, 1,
            new Material(
                ColorAttribute.createDiffuse(Color.WHITE),
                TextureAttribute.createDiffuse(mapTexture)
            ),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates
        );
        mapInstance = new ModelInstance(mapModel);


        // Test model display
        markerModel = modelBuilder.createBox(20f, 20f, 40f, new Material(ColorAttribute.createDiffuse(Color.RED)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        markerInstance = new ModelInstance(markerModel);
        Vector2 markerPos2D = MapRasterTiles.getPixelPosition(MARKER_GEOLOCATION.lat, MARKER_GEOLOCATION.lng, beginTile.x, beginTile.y);
        markerInstance.transform.setTranslation(markerPos2D.x, markerPos2D.y, 0f);

        // Here I make our custom perspective camera that handles movement
        perspectiveCamera = new CustomPerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cameraPosition.set(mapWidth / 2f, mapHeight / 2f, cameraDistance);

        updateCamera();

        // This is here so that the input works
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();

        handleInput(deltaTime);

        updateCamera();

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);
        modelBatch.render(mapInstance, environment);
        modelBatch.render(markerInstance, environment);
        modelBatch.end();
    }

    private void updateCamera() {
        perspectiveCamera.near = 10f;
        perspectiveCamera.far = 8000f;
        perspectiveCamera.setPositionAndDirection(cameraPosition.x, cameraPosition.y, cameraPosition.z, cameraYaw, cameraPitch);
        perspectiveCamera.update();
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        markerModel.dispose();
        for (Texture tile : mapTiles) {
            tile.dispose();
        }
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
        cameraPosition.x = MathUtils.clamp(cameraPosition.x, 0, mapWidth);
        cameraPosition.y = MathUtils.clamp(cameraPosition.y, 0, mapHeight);

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
