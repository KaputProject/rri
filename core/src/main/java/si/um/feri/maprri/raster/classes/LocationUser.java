// java
package si.um.feri.maprri.raster.classes;

import org.json.JSONObject;

import java.util.Objects;

public class LocationUser {
    private final String id;
    private final String username;
    private int numbOftrans;
    private double inflow;
    private double outflow;

    public LocationUser(String id, String username, int numbOftrans, double inflow, double outflow) {
        this.id = Objects.requireNonNull(id, "id");
        this.username = Objects.requireNonNull(username, "username");
        this.numbOftrans = numbOftrans;
        this.inflow = inflow;
        this.outflow = outflow;
    }

    public String getId() { return id; }

    public String getUsername() { return username; }

    public int getNumbOftrans() { return numbOftrans; }

    public double getInflow() { return inflow; }

    public double getOutflow() { return outflow; }
    public void addUserTransaction(LocationUser user) {
        this.inflow = this.inflow + user.getInflow();
        this.outflow = this.outflow + user.getOutflow();
        this.numbOftrans = this.numbOftrans + user.getNumbOftrans();
    }
    public JSONObject toJson() {
        return new JSONObject()
            .put("_id", id)
            .put("username", username)
            .put("numbOftrans", numbOftrans)
            .put("inflow", inflow)
            .put("outflow", outflow);
    }

    @Override
    public String toString() {
        return toJson().toString(2);
    }
}
