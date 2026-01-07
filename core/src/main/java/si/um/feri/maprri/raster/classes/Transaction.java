package si.um.feri.maprri.raster.classes;

import java.util.Objects;

public class Transaction {
    private final String id;
    private final String user;
    private final Location location;
    private final long datetime; // millis since epoch
    private final double change;
    private final boolean outgoing;

    public Transaction(String id, String user, Location location, long datetime, double change, boolean outgoing) {
        this.id = Objects.requireNonNull(id, "id");
        this.user = Objects.requireNonNull(user, "user");
        this.location = Objects.requireNonNull(location, "location");
        this.datetime = datetime;
        this.change = change;
        this.outgoing = outgoing;
    }

    public Transaction(String id, String user, Location location, long datetime) {
        this(id, user, location, datetime, 0.0, true);
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public Location getLocation() {
        return location;
    }

    public long getDatetime() {
        return datetime;
    }

    public double getChange() {
        return change;
    }

    public boolean isOutgoing() {
        return outgoing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return datetime == that.datetime && Double.compare(that.change, change) == 0 && outgoing == that.outgoing && id.equals(that.id) && user.equals(that.user) && location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, location, datetime, change, outgoing);
    }

    @Override
    public String toString() {
        return "Transaction{" + "id='" + id + '\'' + ", user='" + user + '\'' + ", location=" + location + ", datetime=" + datetime + ", change=" + change + ", outgoing=" + outgoing + '}';
    }
}
