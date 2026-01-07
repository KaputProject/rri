package si.um.feri.maprri.raster.classes;

import jdk.internal.net.http.common.Pair;

import java.util.Objects;

public class Location {
    private final String id;
    private final String name;
    private final double lat;
    private final double lng;
    private final String userId;

    public Location(String id, String name, double lat, double lng, String userId) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.lat = lat;
        this.lng = lng;
        this.userId = userId;
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public String getUserId() { return userId; }
    public double getLat() { return lat; }

    public double getLng() { return lng; }

    public Pair<Double, Double> getCoordinates() {
        return new Pair<>(lat, lng);
    }
    @Override
    public String toString() {
        return "Location{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", lat=" + lat +
            ", lng=" + lng +
            ", userId='" + userId + '\'' +
            '}';
    }
}
