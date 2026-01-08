// java
package si.um.feri.maprri.raster.classes;

import jdk.internal.net.http.common.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Location {
    private final String id;
    private final String identifier;
    private final String address;
    private final double lat;
    private final double lng;
    private final List<LocationUser> users;
    private double total_inflow;
    private double total_outflow;
    private int numberOfTrans;

    public Location(String id, String identifier, String address, double lat, double lng, List<LocationUser> users) {
        this.id = Objects.requireNonNull(id, "id");
        this.identifier = Objects.requireNonNull(identifier, "identifier");
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.users = users == null ? List.of() : List.copyOf(users);
    }
    public Location(String id, String identifier, String address, double lat, double lng, List<LocationUser> users, double total_inflow, double total_outflow, int numberOfTrans) {
        this.id = Objects.requireNonNull(id, "id");
        this.identifier = Objects.requireNonNull(identifier, "identifier");
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.users = users == null ? List.of() : List.copyOf(users);
        this.total_inflow = total_inflow;
        this.total_outflow = total_outflow;
        this.numberOfTrans = numberOfTrans;
    }

    public String getId() { return id; }

    public String getName() { return identifier; }

    public String getIdentifier() { return identifier; }

    public String getAddress() { return address; }

    public double getLat() { return lat; }

    public double getLng() { return lng; }

    public double getTotal_inflow() {
        return total_inflow;
    }

    public double getTotal_outflow() {
        return total_outflow;
    }

    public int getNumberOfTrans() {
        return numberOfTrans;
    }

    public LocationUser getUser(String userId) {
        if (userId == null) return null;
        for (LocationUser u : users) {
            if (userId.equals(u.getId()) || userId.equalsIgnoreCase(u.getUsername())) {
                return u;
            }
        }
        return null;
    }


    public List<LocationUser> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public String getUserId() {
        return users.isEmpty() ? null : users.get(0).getId();
    }

    public boolean hasUser(String idOrUsername) {
        if (idOrUsername == null) return false;
        for (LocationUser u : users) {
            if (idOrUsername.equals(u.getId()) || idOrUsername.equalsIgnoreCase(u.getUsername())) return true;
        }
        return false;
    }

    public Pair<Double, Double> getCoordinates() {
        return new Pair<>(lat, lng);
    }

    public JSONObject toJson() {
        JSONArray usersArr = new JSONArray();
        for (LocationUser u : users) {
            usersArr.put(u.toJson());
        }
        return new JSONObject()
            .put("_id", id)
            .put("identifier", identifier)
            .put("address", address)
            .put("lat", lat)
            .put("lng", lng)
            .put("users", usersArr)
            .put("total_inflow", total_inflow)
            .put("total_outflow", total_outflow)
            .put("number_of_transactions", numberOfTrans);
    }

    @Override
    public String toString() {
        return toJson().toString(2);
    }
}
