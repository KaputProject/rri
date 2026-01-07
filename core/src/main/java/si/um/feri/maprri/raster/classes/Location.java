package si.um.feri.maprri.raster.classes;

import java.util.Objects;

public class Location {
    private final String id;
    private final String name;
    private final double lat;
    private final double lng;

    public Location(String id, String name, double lat, double lng) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() { return id; }

    public String getName() { return name; }

    public double getLat() { return lat; }

    public double getLng() { return lng; }

    @Override
    public String toString() {
        return "Location{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", lat=" + lat +
            ", lng=" + lng +
            '}';
    }
}
