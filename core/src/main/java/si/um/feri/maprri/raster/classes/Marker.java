// java
package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import si.um.feri.maprri.raster.utils.Geolocation;
import si.um.feri.maprri.raster.utils.MapRasterTiles;

public class Marker {
    private SimulatedTransaction simulatedTransaction;
    private Geolocation location;
    private Model markerModel;
    private Vector2 markerPos2D;
    protected ModelInstance markerInstance;
    private float hitboxRadius = 10f; // adjustable radius in world/pixel units
    public BoundingBox getHitboxBoundingBox() {
        Vector2 pos = getPixelPosition();
        float radius = getHitboxRadius();
        float size = radius * 2f;
        float half = size / 2f;
        float minX = pos.x - half, maxX = pos.x + half;
        float minY = pos.y - half, maxY = pos.y + half;
        float bottomZ = 0f, topZ = 100f; // Default height
        return new BoundingBox(new Vector3(minX, minY, bottomZ), new Vector3(maxX, maxY, topZ));
    }

    public boolean intersectsRay(Ray ray, Vector3 intersection) {
        return Intersector.intersectRayBounds(ray, getHitboxBoundingBox(), intersection);
    }
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

    public void render(ModelBatch batch, Environment environment){
        batch.render(markerInstance, environment);
    }

    public ModelInstance getInstance() {
        return markerInstance;
    }

    public boolean containsPoint1(double x, double y) {
        if (markerPos2D == null) return false;
        float dx = markerPos2D.x - (float) x;
        float dy = markerPos2D.y - (float) y;
        return dx * dx + dy * dy <= hitboxRadius * hitboxRadius;
    }

    // New getters to allow drawing the hitbox overlay
    public Vector2 getPixelPosition() {
        return markerPos2D;
    }

    public float getHitboxRadius() {
        return hitboxRadius;
    }

    public void dispose() {
        if (markerModel != null) {
            markerModel.dispose();
        }
    }

}
