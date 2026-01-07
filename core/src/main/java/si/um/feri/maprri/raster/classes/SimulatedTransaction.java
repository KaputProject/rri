// java
package si.um.feri.maprri.raster.classes;

import org.json.JSONObject;

import java.time.Instant;
import java.util.Objects;
// A simulated transaction at a specific location and time (also location read form notification)
public class SimulatedTransaction {
    private final Location location;
    private final long datetime; // millis since epoch

    public SimulatedTransaction(String id, Location location, long datetime) {
        this.location = Objects.requireNonNull(location, "location");
        this.datetime = datetime;
    }


    public Location getLocation() { return location; }

    public long getDatetime() { return datetime; }

    public JSONObject toJson() {
        return new JSONObject()
            .put("datetime", Instant.ofEpochMilli(datetime).toString())
            .put("location", location.toJson());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimulatedTransaction)) return false;
        SimulatedTransaction that = (SimulatedTransaction) o;
        return datetime == that.datetime && location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, datetime);
    }

    @Override
    public String toString() {
        return toJson().toString(2);
    }
}
