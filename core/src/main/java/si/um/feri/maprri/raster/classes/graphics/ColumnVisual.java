package si.um.feri.maprri.raster.classes.graphics;

import si.um.feri.maprri.raster.classes.Location;

public class ColumnVisual {
    public final Location location;
    public final String userId;
    public final double value;
    public final ColumnMode mode;

    public ColumnVisual(Location location, String userId, double value, ColumnMode mode) {
        this.location = location;
        this.userId = userId;
        this.value = value;
        this.mode = mode;
    }

    public double getLat() {
        return location.getLat();
    }

    public double getLng() {
        return location.getLng();
    }

    public String getLocationId() {
        return location.getId();
    }

    public String getLocationName() {
        return location.getIdentifier();
    }
}

