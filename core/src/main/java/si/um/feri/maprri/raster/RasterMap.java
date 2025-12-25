package si.um.feri.maprri.raster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.IOException;

import si.um.feri.maprri.raster.config.Config;
import si.um.feri.maprri.raster.utils.Geolocation;
import si.um.feri.maprri.raster.utils.MapRasterTiles;
import si.um.feri.maprri.raster.utils.ZoomXY;

public class RasterMap extends ApplicationAdapter implements GestureDetector.GestureListener {
    private Vector3 touchPosition;

    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    private ModelBatch modelBatch;
    private PerspectiveCamera perspectiveCamera;
    private ModelInstance markerInstance;
    private Model markerModel;
    private Environment environment;

    private Texture[] mapTiles;
    private ZoomXY beginTile;

    private final Geolocation CENTER_GEOLOCATION = new Geolocation(46.557314, 15.637771);
    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);

    @Override
    public void create() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Config.MAP_WIDTH / 2f, Config.MAP_HEIGHT / 2f, 0);
        camera.zoom = Config.INITIAL_ZOOM;
        camera.update();

        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(CENTER_GEOLOCATION.lat, CENTER_GEOLOCATION.lng, Config.ZOOM);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Config.NUM_TILES);
            beginTile = new ZoomXY(Config.ZOOM, centerTile.x - ((Config.NUM_TILES - 1) / 2), centerTile.y - ((Config.NUM_TILES - 1) / 2));
        } catch (IOException e) {
            e.printStackTrace();
        }

        tiledMap = new TiledMap();
        MapLayers layers = tiledMap.getLayers();
        TiledMapTileLayer layer = new TiledMapTileLayer(Config.NUM_TILES, Config.NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);
        int index = 0;
        for (int j = Config.NUM_TILES - 1; j >= 0; j--) {
            for (int i = 0; i < Config.NUM_TILES; i++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                layer.setCell(i, j, cell);
                index++;
            }
        }
        layers.add(layer);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        ModelBuilder modelBuilder = new ModelBuilder();
        markerModel = modelBuilder.createBox(20f, 20f, 20f, new Material(ColorAttribute.createDiffuse(com.badlogic.gdx.graphics.Color.RED)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);

        markerInstance = new ModelInstance(markerModel);

        Vector2 markerPos2D = MapRasterTiles.getPixelPosition(MARKER_GEOLOCATION.lat, MARKER_GEOLOCATION.lng, beginTile.x, beginTile.y);
        markerInstance.transform.setTranslation(markerPos2D.x, markerPos2D.y, 10f);

        perspectiveCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        perspectiveCamera.position.set(Config.MAP_WIDTH / 2f, Config.MAP_HEIGHT / 2f, 600f);
        perspectiveCamera.lookAt(Config.MAP_WIDTH / 2f, Config.MAP_HEIGHT / 2f, 0);
        perspectiveCamera.near = 1f;
        perspectiveCamera.far = 4000f;
        perspectiveCamera.update();

        this.touchPosition = new Vector3();
        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    @Override
    public void render() {
        handleInput();
        camera.update();

        ScreenUtils.clear(0, 0, 0, 1);
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        syncCameras();

        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);
        modelBatch.render(markerInstance, environment);
        modelBatch.end();
    }

    private void syncCameras() {
        perspectiveCamera.position.x = camera.position.x;
        perspectiveCamera.position.y = camera.position.y;
        perspectiveCamera.position.z = 600f * camera.zoom;
        perspectiveCamera.lookAt(camera.position.x, camera.position.y, 0);
        perspectiveCamera.update();
    }

    @Override
    public void dispose() {
        tiledMap.dispose();
        modelBatch.dispose();
        markerModel.dispose();
        for (Texture tile : mapTiles) {
            tile.dispose();
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        if (initialDistance >= distance)
            camera.zoom += 0.04f;
        else
            camera.zoom -= 0.04f;
        return false;
    }


    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom += 0.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= 0.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.translate(-Config.CAMERA_MOVEMENT_SPEED * camera.zoom, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            camera.translate(Config.CAMERA_MOVEMENT_SPEED * camera.zoom, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            camera.translate(0, -Config.CAMERA_MOVEMENT_SPEED * camera.zoom, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            camera.translate(0, Config.CAMERA_MOVEMENT_SPEED * camera.zoom, 0);
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f);

        float effectiveViewportWidth = camera.viewportWidth * camera.zoom;
        float effectiveViewportHeight = camera.viewportHeight * camera.zoom;

        if (effectiveViewportWidth > Config.MAP_WIDTH) {
            camera.position.x = Config.MAP_WIDTH / 2f;
        } else {
            camera.position.x = MathUtils.clamp(camera.position.x, effectiveViewportWidth / 2f, Config.MAP_WIDTH - effectiveViewportWidth / 2f);
        }

        if (effectiveViewportHeight > Config.MAP_HEIGHT) {
            camera.position.y = Config.MAP_HEIGHT / 2f;
        } else {
            camera.position.y = MathUtils.clamp(camera.position.y, effectiveViewportHeight / 2f, Config.MAP_HEIGHT - effectiveViewportHeight / 2f);
        }
    }
}
