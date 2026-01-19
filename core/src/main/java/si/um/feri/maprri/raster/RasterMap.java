package si.um.feri.maprri.raster;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import org.json.JSONObject;
import si.um.feri.maprri.raster.classes.*;
import si.um.feri.maprri.raster.classes.graphics.ColumnMarker;
import si.um.feri.maprri.raster.classes.graphics.ColumnMode;
import si.um.feri.maprri.raster.config.Config;
import si.um.feri.maprri.raster.manager.ColumnManager;
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
    private HudView hudView;
    private float debugToggleCooldown = 0f;
    private boolean debugMode = false;
    private Map map;
    private boolean invertMouse = false;
    private Vector3 cameraPosition = new Vector3();
    private float cameraPitch = Config.INITIAL_PITCH;
    private float cameraYaw = 0f;
    private float cameraDistance = Config.CAMERA_Z_INITIAL;
    private DataManager dataManager;
    private ColumnManager columnManager;
    private LocationScheduler locationScheduler;
    private float maxHeight = 300f;
    private ShapeRenderer shapeRenderer;
    private static boolean familyView = true; // Default to family view (matches ColumnManager.currentUserId=null)
    private static boolean showMarkerLabels = true;

    private final Geolocation MARKER_GEOLOCATION = new Geolocation(46.559070, 15.638100);


    @Override
    public void create() {
        JSONObject testData = new JSONObject("{\"message\":\"Family statistics retrieved successfully\",\"familyMembers\":[{\"_id\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"},{\"_id\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\"}],\"statistics\":[{\"_id\":\"68517eb03dc2b1787d66f15a\",\"name\":\"MCDONALDS SWATY\",\"lat\":46.5417236,\"lng\":15.6477553,\"address\":\"Tržaška cesta 6, 2000 Maribor, Slovenija\",\"identifier\":\"MCDONALDS SWATY\",\"total_inflow\":0,\"total_outflow\":309.68,\"number_of_transactions\":16,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":16,\"inflow\":0,\"outflow\":309.68}]},{\"_id\":\"6851807a3dc2b1787d66ff29\",\"name\":\"TELEFIX D.O.O.\",\"lat\":46.0610445,\"lng\":14.6145194,\"address\":\"Zaloška cesta 269, 1000 Ljubljana, Slovenija\",\"identifier\":\"TELEFIX D.O.O.\",\"total_inflow\":390,\"total_outflow\":0,\"number_of_transactions\":1,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":1,\"inflow\":390,\"outflow\":0}]},{\"_id\":\"68517fde3dc2b1787d66fe16\",\"name\":\"ENEJ K.\",\"lat\":46.561514516727684,\"lng\":15.630581937335277,\"address\":\"Smetanova ulica 71, 2000 Maribor, Slovenija\",\"identifier\":\"ENEJ K.\",\"total_inflow\":0,\"total_outflow\":1042.2599999999998,\"number_of_transactions\":37,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":37,\"inflow\":0,\"outflow\":1042.2599999999998}]},{\"_id\":\"68517f403dc2b1787d66fc79\",\"name\":\"KRAMAR ENEJ\",\"lat\":46.561583768170415,\"lng\":15.630117235645868,\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"KRAMAR ENEJ\",\"total_inflow\":4072.360000000001,\"total_outflow\":40,\"number_of_transactions\":59,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":59,\"inflow\":4072.360000000001,\"outflow\":40}]},{\"_id\":\"68517f643dc2b1787d66fcf3\",\"name\":\"DAVID G.\",\"lat\":46.56155984639398,\"lng\":15.630197525024414,\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"DAVID G.\",\"total_inflow\":610.9199999999998,\"total_outflow\":151.26000000000002,\"number_of_transactions\":79,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":79,\"inflow\":610.9199999999998,\"outflow\":151.26000000000002}]},{\"_id\":\"6851822a3dc2b1787d670639\",\"name\":\"GREGOR V.\",\"lat\":46.563884637316235,\"lng\":15.625278829936864,\"address\":\"Gosposvetska Cesta 87, 2000 Maribor, Slovenija\",\"identifier\":\"GREGOR V.\",\"total_inflow\":233.02000000000004,\"total_outflow\":33.34,\"number_of_transactions\":33,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":33,\"inflow\":233.02000000000004,\"outflow\":33.34}]},{\"_id\":\"6851805c3dc2b1787d66fefe\",\"name\":\"ASPIRIA d.o.o.\",\"lat\":46.23498990181822,\"lng\":15.277884006500244,\"address\":\"Kidričeva ulica 24 a, 3000 Celje, Slovenija\",\"identifier\":\"ASPIRIA d.o.o.\",\"total_inflow\":4600,\"total_outflow\":0,\"number_of_transactions\":8,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":8,\"inflow\":4600,\"outflow\":0}]},{\"_id\":\"68517f523dc2b1787d66fcae\",\"name\":\"TILEN Z.\",\"lat\":46.56158101599449,\"lng\":15.630207259893885,\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"TILEN Z.\",\"total_inflow\":327.65999999999997,\"total_outflow\":149.6,\"number_of_transactions\":50,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":50,\"inflow\":327.65999999999997,\"outflow\":149.6}]},{\"_id\":\"68517f9e3dc2b1787d66fd9a\",\"name\":\"LANA K.\",\"lat\":46.547915917590664,\"lng\":15.645801542066534,\"address\":\"Ljubljanska ulica 29-31,33, 2000 Maribor, Slovenija\",\"identifier\":\"LANA K.\",\"total_inflow\":2059.8600000000006,\"total_outflow\":1913.26,\"number_of_transactions\":68,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":68,\"inflow\":2059.8600000000006,\"outflow\":1913.26},{\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\",\"numbOfTrans\":0,\"inflow\":0,\"outflow\":0}]},{\"_id\":\"685182b03dc2b1787d6709d9\",\"name\":\"RESTAVRACIJA MANGO\",\"lat\":46.56310800000001,\"lng\":15.6295763,\"address\":\"Turnerjeva ulica 17, 2000 Maribor, Slovenija\",\"identifier\":\"RESTAVRACIJA MANGO\",\"total_inflow\":0,\"total_outflow\":852.4000000000002,\"number_of_transactions\":23,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":23,\"inflow\":0,\"outflow\":852.4000000000002}]},{\"_id\":\"685181533dc2b1787d6703de\",\"name\":\"Lidl Slovenija d o o\",\"lat\":46.191395,\"lng\":14.519102,\"address\":\"Pod Lipami 1, 1218 Komenda, Slovenija\",\"identifier\":\"Lidl Slovenija d o o\",\"total_inflow\":0,\"total_outflow\":206.45999999999998,\"number_of_transactions\":6,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":6,\"inflow\":0,\"outflow\":206.45999999999998}]},{\"_id\":\"6851811a3dc2b1787d67002a\",\"name\":\"TELEKOM SLOVENIJE\",\"lat\":46.085665,\"lng\":14.4825975,\"address\":\"Stegne 19, 1000 Ljubljana, Slovenija\",\"identifier\":\"TELEKOM SLOVENIJE\",\"total_inflow\":0,\"total_outflow\":720.86,\"number_of_transactions\":7,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":7,\"inflow\":0,\"outflow\":720.86}]},{\"_id\":\"68517f7e3dc2b1787d66fd44\",\"name\":\"URBAN L.\",\"lat\":46.56021282728087,\"lng\":15.633699189118898,\"address\":\"Kočevarjeva ulica 6a, 2000 Maribor, Slovenija\",\"identifier\":\"URBAN L.\",\"total_inflow\":97.25999999999999,\"total_outflow\":26.86,\"number_of_transactions\":17,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":17,\"inflow\":97.25999999999999,\"outflow\":26.86}]},{\"_id\":\"68517f8e3dc2b1787d66fd6f\",\"name\":\"PEKARNA IN SLASCICARNA\",\"lat\":46.1347198,\"lng\":14.5724277,\"address\":\"Depala vas 93a, 1230 Domžale, Slovenija\",\"identifier\":\"PEKARNA IN SLASCICARNA\",\"total_inflow\":0,\"total_outflow\":136.39999999999998,\"number_of_transactions\":16,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":16,\"inflow\":0,\"outflow\":136.39999999999998}]},{\"_id\":\"68517fae3dc2b1787d66fdcf\",\"name\":\"BESTERSI MAS*MM PANDA\",\"lat\":46.5577136,\"lng\":15.6431623,\"address\":\"Koroška cesta 9, 2000 Maribor, Slovenija\",\"identifier\":\"BESTERSI MAS*MM PANDA\",\"total_inflow\":0,\"total_outflow\":203,\"number_of_transactions\":22,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":22,\"inflow\":0,\"outflow\":203}]},{\"_id\":\"68517ff03dc2b1787d66fe41\",\"name\":\"LEKARNA TABOR\",\"lat\":46.5533756,\"lng\":15.6466423,\"address\":\"Ljubljanska ulica 9, 2000 Maribor, Slovenija\",\"identifier\":\"LEKARNA TABOR\",\"total_inflow\":0,\"total_outflow\":342.88,\"number_of_transactions\":2,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":2,\"inflow\":0,\"outflow\":342.88}]},{\"_id\":\"685180c63dc2b1787d66ff70\",\"name\":\"SPAR P KOROSKA MB\",\"lat\":46.55901124872605,\"lng\":15.636086405832245,\"address\":\"Koroška cesta 53b, 2000 Maribor, Slovenija\",\"identifier\":\"SPAR P KOROSKA MB\",\"total_inflow\":0,\"total_outflow\":114.80000000000001,\"number_of_transactions\":15,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":15,\"inflow\":0,\"outflow\":114.80000000000001}]},{\"_id\":\"685180d93dc2b1787d66ff9b\",\"name\":\"ZAVERSKI\",\"lat\":46.5449185,\"lng\":15.6486344,\"address\":\"Titova cesta 48, 2000 Maribor, Slovenija\",\"identifier\":\"ZAVERSKI\",\"total_inflow\":0,\"total_outflow\":474.1,\"number_of_transactions\":1,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":1,\"inflow\":0,\"outflow\":474.1}]},{\"_id\":\"68517ffd3dc2b1787d66fe6c\",\"name\":\"MCDONALDS ZELEZNISKA\",\"lat\":46.057932,\"lng\":14.5105647,\"address\":\"Trg Osvobodilne fronte 6, 1000 Ljubljana, Slovenija\",\"identifier\":\"MCDONALDS ZELEZNISKA\",\"total_inflow\":0,\"total_outflow\":5.6,\"number_of_transactions\":1,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":1,\"inflow\":0,\"outflow\":5.6}]},{\"_id\":\"6851800b3dc2b1787d66fe97\",\"name\":\"LEKARNA BTC\",\"lat\":46.6696243,\"lng\":16.1781905,\"address\":\"Nemčavci 1, 9000 Murska Sobota, Slovenija\",\"identifier\":\"LEKARNA BTC\",\"total_inflow\":0,\"total_outflow\":33.94,\"number_of_transactions\":1,\"users\":[{\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\",\"numbOfTrans\":1,\"inflow\":0,\"outflow\":33.94}]},{\"_id\":\"695d45101623c31ca33fb782\",\"name\":\"Lawadanci\",\"lat\":46.1,\"lng\":46.2,\"address\":\"hmmmm nasadaloc juppp\",\"identifier\":\"rnemki.\",\"total_inflow\":0,\"total_outflow\":0,\"number_of_transactions\":0,\"users\":[{\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\",\"numbOfTrans\":0,\"inflow\":0,\"outflow\":0}]}]}");
        String familyData = "[{\"_id\":\"c58c84e0-25d4-479d-aab6-1b17345de17f\",\"datetime\":1768768777331,\"location\":{\"_id\":\"68517fde3dc2b1787d66fe16\",\"address\":\"Smetanova ulica 71, 2000 Maribor, Slovenija\",\"identifier\":\"ENEJ K.\",\"lat\":46.561514516727684,\"lng\":15.630581937335277,\"name\":\"ENEJ K.\",\"numbOfTrans\":1,\"total_inflow\":464.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":464.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"feb456c7-6041-462f-a5bb-f707fcbc7f3e\",\"datetime\":1768768770331,\"location\":{\"_id\":\"68517f523dc2b1787d66fcae\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"TILEN Z.\",\"lat\":46.56158101599449,\"lng\":15.630207259893885,\"name\":\"TILEN Z.\",\"numbOfTrans\":1,\"total_inflow\":441.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":441.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"dea97f35-9599-4e03-b136-6314357e9a42\",\"datetime\":1768768750331,\"location\":{\"_id\":\"68517fde3dc2b1787d66fe16\",\"address\":\"Smetanova ulica 71, 2000 Maribor, Slovenija\",\"identifier\":\"ENEJ K.\",\"lat\":46.561514516727684,\"lng\":15.630581937335277,\"name\":\"ENEJ K.\",\"numbOfTrans\":1,\"total_inflow\":435.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":435.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\"}]}},{\"_id\":\"af029580-6d51-42cf-bec0-cf74b89a6c5e\",\"datetime\":1768768758331,\"location\":{\"_id\":\"68517f523dc2b1787d66fcae\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"TILEN Z.\",\"lat\":46.56158101599449,\"lng\":15.630207259893885,\"name\":\"TILEN Z.\",\"numbOfTrans\":1,\"total_inflow\":605.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":605.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"b7634d8a-0a2d-40c0-b1e9-b45e4d4b4e4a\",\"datetime\":1768768776331,\"location\":{\"_id\":\"68517f403dc2b1787d66fc79\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"KRAMAR ENEJ\",\"lat\":46.561583768170415,\"lng\":15.630117235645868,\"name\":\"KRAMAR ENEJ\",\"numbOfTrans\":1,\"total_inflow\":28.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":28.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"b7c15c1c-dfea-4ba4-8628-20c794eabf2b\",\"datetime\":1768768787331,\"location\":{\"_id\":\"68517f643dc2b1787d66fcf3\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"DAVID G.\",\"lat\":46.56155984639398,\"lng\":15.630197525024414,\"name\":\"DAVID G.\",\"numbOfTrans\":1,\"total_inflow\":759.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":759.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\"}]}},{\"_id\":\"09725030-9b00-452d-a13d-68b31a715994\",\"datetime\":1768768775331,\"location\":{\"_id\":\"68517f403dc2b1787d66fc79\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"KRAMAR ENEJ\",\"lat\":46.561583768170415,\"lng\":15.630117235645868,\"name\":\"KRAMAR ENEJ\",\"numbOfTrans\":1,\"total_inflow\":742.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":742.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"4b338233-53de-47c6-ba6f-a5292d32e233\",\"datetime\":1768768792331,\"location\":{\"_id\":\"68517fde3dc2b1787d66fe16\",\"address\":\"Smetanova ulica 71, 2000 Maribor, Slovenija\",\"identifier\":\"ENEJ K.\",\"lat\":46.561514516727684,\"lng\":15.630581937335277,\"name\":\"ENEJ K.\",\"numbOfTrans\":1,\"total_inflow\":909.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":909.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"14ccf1ea-fe29-46bd-a86a-286cb8f44b1d\",\"datetime\":1768768773331,\"location\":{\"_id\":\"68517f403dc2b1787d66fc79\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"KRAMAR ENEJ\",\"lat\":46.561583768170415,\"lng\":15.630117235645868,\"name\":\"KRAMAR ENEJ\",\"numbOfTrans\":1,\"total_inflow\":16.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":16.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"97491b4c-6922-4d88-af91-ec1bdcc32daf\",\"datetime\":1768768758331,\"location\":{\"_id\":\"68517f643dc2b1787d66fcf3\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"DAVID G.\",\"lat\":46.56155984639398,\"lng\":15.630197525024414,\"name\":\"DAVID G.\",\"numbOfTrans\":1,\"total_inflow\":705.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":705.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\"}]}},{\"_id\":\"da94b09c-dff6-4a01-89ef-6a4b91674b3a\",\"datetime\":1768768764331,\"location\":{\"_id\":\"68517f643dc2b1787d66fcf3\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"DAVID G.\",\"lat\":46.56155984639398,\"lng\":15.630197525024414,\"name\":\"DAVID G.\",\"numbOfTrans\":1,\"total_inflow\":844.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":844.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\"}]}},{\"_id\":\"c21418b2-e56c-4e2e-82b5-fc72adeb522d\",\"datetime\":1768768757331,\"location\":{\"_id\":\"68517f523dc2b1787d66fcae\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"TILEN Z.\",\"lat\":46.56158101599449,\"lng\":15.630207259893885,\"name\":\"TILEN Z.\",\"numbOfTrans\":1,\"total_inflow\":131.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":131.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"33ea474c-6bd5-4268-aace-a225bbbb4e8e\",\"datetime\":1768768752331,\"location\":{\"_id\":\"68517f643dc2b1787d66fcf3\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"DAVID G.\",\"lat\":46.56155984639398,\"lng\":15.630197525024414,\"name\":\"DAVID G.\",\"numbOfTrans\":1,\"total_inflow\":475.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":475.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"685160a14f2c91b527966287\",\"username\":\"LukaKuder\"}]}},{\"_id\":\"ee086b2a-64aa-4b55-acaf-95cd493eb46f\",\"datetime\":1768768765331,\"location\":{\"_id\":\"68517f643dc2b1787d66fcf3\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"DAVID G.\",\"lat\":46.56155984639398,\"lng\":15.630197525024414,\"name\":\"DAVID G.\",\"numbOfTrans\":1,\"total_inflow\":458.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":458.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\"}]}},{\"_id\":\"854743bb-2393-43dc-b2d0-b02041b62cc4\",\"datetime\":1768768771331,\"location\":{\"_id\":\"68517f403dc2b1787d66fc79\",\"address\":\"Smetanova ulica 75, 2000 Maribor, Slovenija\",\"identifier\":\"KRAMAR ENEJ\",\"lat\":46.561583768170415,\"lng\":15.630117235645868,\"name\":\"KRAMAR ENEJ\",\"numbOfTrans\":1,\"total_inflow\":332.0,\"total_outflow\":0.0,\"users\":[{\"inflow\":332.0,\"numbOfTrans\":1,\"outflow\":0.0,\"userId\":\"695d0d9a23be6550bc43d241\",\"username\":\"testni1\"}]}}]";
        map = new Map();
        mqttUtil = new MqttUtil();
        httpUtil = new HttpUtil();
        dataManager = new DataManager();
        // TODO: Tule je demonstracija povezave, lahk si prilagodita se dodatne funkcije al pa backend ce je ka treba, js se ne vem ker pac vidva bota pol vidla kake podatke rabita
        dataManager.loadBaseData(HttpUtil.getBaseLocationData(),HttpUtil.getFamilyId());
        dataManager.loadBaseData(testData.toString(), "685160a14f2c91b527966287");


        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.65f, 0.65f, 0.65f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, -1f, -0.3f));

//        dataManager.addMarker(new Marker(46.559070, 15.638100, map));
//        dataManager.addMarker(new Marker(46.560000, 15.640000, map));
//        dataManager.addMarker(new Marker(46.558000, 15.636000, map));

        columnManager = new ColumnManager(dataManager, map, maxHeight);
        columnManager.rebuild();

        // Here I make our custom perspective camera that handles movement
        perspectiveCamera = new CustomPerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        int mapWidth = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        int mapHeight = Config.NUM_TILES * MapRasterTiles.TILE_SIZE;
        cameraPosition.set(mapWidth / 2f, mapHeight / 2f, cameraDistance);
        locationScheduler = new LocationScheduler(dataManager, columnManager);
        updateCamera();

        modelBatch = new ModelBatch();
        hudView = new HudView(dataManager);
        hudView.create();
        hudView.setFamilyMode(familyView); // Sync HUD label with initial familyView state

        // Register filter change listener
        hudView.setFilterChangeListener(new HudView.FilterChangeListener() {
            @Override
            public void onModeChanged(ColumnMode mode) {
                columnManager.setMode(mode);
            }

            @Override
            public void onUserChanged(String userId) {
                familyView = (userId == null);
                hudView.setFamilyMode(familyView);
                columnManager.setUserId(userId);
            }

            @Override
            public void onMinAmountChanged(float minAmount) {
                columnManager.setMinAmount(minAmount);
            }
        });

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudView.getStage());
        multiplexer.addProcessor(new GestureDetector(this));
        Gdx.input.setInputProcessor(multiplexer);

        shapeRenderer = new ShapeRenderer();
        //dataManager.extractTransactionsFromSimulateJson(familyData);
        //locationScheduler.updateTransactions(dataManager.getTransactionsByType("simulate"));
        initMqttListeners();
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        locationScheduler.processAllDue();
        handleInput(deltaTime);
        update(deltaTime);
        draw();
    }

    private void update(float delta) {
        updateCamera();
        map.update(cameraPosition);
        columnManager.update(delta);
    }

    private void draw() {
        // Sky-blue background
        Gdx.gl.glClearColor(0.53f, 0.81f, 0.92f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(perspectiveCamera);

        for (ModelInstance tileInstance : map.tileInstances.values()) {
            modelBatch.render(tileInstance, environment);
        }
        List<Marker> markers = dataManager.getMarkers();
        for (Marker m : markers) {
            modelBatch.render(m.getInstance(), environment);
        }
        columnManager.render(modelBatch, environment);

        modelBatch.end();

        // Render location labels as billboard text (after 3D rendering)
        columnManager.renderLabels(perspectiveCamera);

        shapeRenderer.setProjectionMatrix(perspectiveCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (debugMode){
            columnManager.renderHitboxes(shapeRenderer);
        }
        shapeRenderer.end();

        hudView.render();
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
        if (hudView != null) hudView.dispose();
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        float pitchDelta = invertMouse ? -deltaX : deltaX;
        cameraPitch -= deltaY * Config.CAMERA_MOUSE_PITCH_SPEED;
        cameraPitch = MathUtils.clamp(cameraPitch, Config.MIN_PITCH, Config.MAX_PITCH);

        cameraYaw -= pitchDelta * Config.CAMERA_MOUSE_YAW;
        cameraYaw = (cameraYaw + 360f) % 360f;
        return true;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        Ray pickRay = perspectiveCamera.getPickRay(x, y);
        Vector3 intersection = new Vector3();
        ColumnMarker hit = columnManager.getHitColumn(pickRay, intersection);
        if (hit != null) {
            Location location = hit.getVisual().getLocation();
            if (familyView) {
                hudView.showDetailsForFamily(location);
            } else {
                String userId = dataManager.mainUser.get(0).getId();
                hudView.showDetailsForUser(location, userId);
            }
            return true;
        }
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
        cameraPosition.z *= (initialDistance / distance);
        cameraPosition.z = MathUtils.clamp(cameraPosition.z, 400, 3000);
        return true;
    }

    public void resize(int width, int height) {
        perspectiveCamera.viewportWidth = width;
        perspectiveCamera.viewportHeight = height;
        perspectiveCamera.update();

        if (hudView != null) hudView.resize(width, height);
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

        debugToggleCooldown = Math.max(0f, debugToggleCooldown - delta);
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            if (debugToggleCooldown <= 0f) {
                debugMode = !debugMode;
                debugToggleCooldown = 0.5f;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            invertMouse = !invertMouse;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            familyView = !familyView;
            hudView.setFamilyMode(familyView);
            updateColumnFilter();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            showMarkerLabels = !showMarkerLabels;
        }
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
                locationScheduler.updateTransactions(dataManager.getTransactionsByType("simulate"));
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updateColumnFilter() {
        if (familyView) {
            columnManager.setUserId(null); // family view
        } else {
            if (!dataManager.mainUser.isEmpty()) {
                columnManager.setUserId(dataManager.mainUser.get(0).getId());
            }
        }
    }
    public static boolean isFamilyView() {
        return familyView;
    }
    // Za spremembo filtra (npr. iz UI)
    public void onFilterChanged(ColumnMode mode, String userId) {
        columnManager.setMode(mode);
        columnManager.setUserId(userId);
    }

    // Ob real-time posodobitvi podatkov
    public void onSimulationUpdate() {
        columnManager.onDataUpdated();
    }
    public static boolean isShowMarkerLabels() {
        return showMarkerLabels;
    }

    public static void setShowMarkerLabels(boolean showMarkerLabels) {
        RasterMap.showMarkerLabels = showMarkerLabels;
    }


}
