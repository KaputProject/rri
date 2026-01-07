package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector2;
import si.um.feri.maprri.raster.utils.Geolocation;
import si.um.feri.maprri.raster.utils.MapRasterTiles;

public class Marker {
    private SimulatedTransaction simulatedTransaction;
    private Geolocation location;
    private Model markerModel;
    private Vector2 markerPos2D;
    private ModelInstance markerInstance;

    public Marker(SimulatedTransaction simulatedTransaction, Map map) {
        this.simulatedTransaction = simulatedTransaction;
        this.location = new Geolocation(
            simulatedTransaction.getLocation().getLat(),
            simulatedTransaction.getLocation().getLng()
        );
        initModelAndPosition(map);
    }

    public Marker(double lat, double lng, Map map) {
        this.location = new Geolocation(lat, lng);
        initModelAndPosition(map);
    }

    private void initModelAndPosition(Map map) {
        ModelBuilder modelBuilder = new ModelBuilder();
        markerModel = modelBuilder.createBox(
            20f, 20f, 40f,
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
        markerInstance = new ModelInstance(markerModel);
        markerPos2D = MapRasterTiles.getPixelPosition(
            location.lat,
            location.lng,
            map.beginTile.x,
            map.beginTile.y
        );
        markerInstance.transform.setTranslation(markerPos2D.x, markerPos2D.y, 1f);
    }

    public ModelInstance getInstance() {
        return markerInstance;
    }

    public void dispose() {
        if (markerModel != null) {
            markerModel.dispose();
        }
    }

}
