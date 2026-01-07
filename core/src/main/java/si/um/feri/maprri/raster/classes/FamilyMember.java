package si.um.feri.maprri.raster.classes;

public class FamilyMember {
    private final String id;
    private final String name;

    public FamilyMember(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "FamilyMember{id='" + id + "', name='" + name + "'}";
    }
}
