package si.um.feri.maprri.raster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import si.um.feri.maprri.raster.classes.*;
import si.um.feri.maprri.raster.config.Config;
import si.um.feri.maprri.raster.manager.DataManager;
import si.um.feri.maprri.raster.utils.Geolocation;
import si.um.feri.maprri.raster.utils.HttpUtil;
import si.um.feri.maprri.raster.utils.MapRasterTiles;
import si.um.feri.maprri.raster.utils.MqttUtil;

import java.util.List;

public class RasterMap extends ApplicationAdapter implements GestureDetector.GestureListener {
    private ModelBatch modelBatch;
    private CustomPerspectiveCamera perspectiveCamera;
    private ModelInstance markerInstance;
    private Model markerModel;
    private Environment environment;
    private MqttUtil mqttUtil;
    private HttpUtil httpUtil;

    private Map map;

    private Vector3 cameraPosition = new Vector3();
    private float cameraPitch = Config.INITIAL_PITCH;
    private float cameraYaw = 0f;
    private float cameraDistance = Config.CAMERA_Z_INITIAL;

    private DataManager dataManager;

    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);

    @Override
    public void create() {
        map = new Map();
        mqttUtil = new MqttUtil();
        httpUtil = new HttpUtil();
        dataManager = new DataManager();
        // TODO: Tule je demonstracija povezave, lahk si prilagodita se dodatne funkcije al pa backend ce je ka treba, js se ne vem ker pac vidva bota pol vidla kake podatke rabita
        dataManager.loadBaseData(HttpUtil.getBaseLocationData(),HttpUtil.getFamilyId());
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.65f, 0.65f, 0.65f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.3f));

        dataManager.addMarker(new Marker(46.559070, 15.638100, map));
        dataManager.addMarker(new Marker(46.560000, 15.640000, map));
        dataManager.addMarker(new Marker(46.558000, 15.636000, map));

        // Here I make our custom perspective camera that handles movement
        perspectiveCamera = new CustomPerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        int mapWidth = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        int mapHeight = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        cameraPosition.set(mapWidth / 2f, mapHeight / 2f, cameraDistance);

        updateCamera();

        modelBatch = new ModelBatch();

        // This is here so that the input works
        Gdx.input.setInputProcessor(new GestureDetector(this));

        initMqttListeners();
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
        map.update(cameraPosition);
    }

    private void draw() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);

        for (ModelInstance tileInstance : map.tileInstances.values()) {
            modelBatch.render(tileInstance, environment);
        }
        //dobimo markerje in jih narišemo
        List<Marker> markers = dataManager.getMarkers();
        for (Marker m : markers) {
            modelBatch.render(m.getInstance(), environment);
        }


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
        for (Marker m : dataManager.getMarkers()) {
            m.dispose();
        }
        map.dispose();
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

    private void initMqttListeners() {
        try {
            // TODO: Tuki implementiraj evente oz simulacije iz androida
            mqttUtil.client.subscribe("kaput/event", (topic, msg) -> {
                String message = new String(msg.getPayload());
                System.out.println("Received MQTT message on topic " + topic + ": " + message);
            });

            mqttUtil.client.subscribe("kaput/simulate", (topic, msg) -> {
                String message = new String(msg.getPayload());
                System.out.println("Received MQTT message on topic " + topic + ": " + message);
                dataManager.extractTransactionsFromSimulateJson(message);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
