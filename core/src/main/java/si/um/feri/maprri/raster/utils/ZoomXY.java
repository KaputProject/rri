package si.um.feri.maprri.raster.utils;

public class ZoomXY {
    public int zoom;
    public int x;
    public int y;

    public ZoomXY(int zoom, int x, int y) {
        this.zoom = zoom;
        this.x = x;
        this.y = y;
    }

    public String toString() {
        return zoom + "/" + x + "/" + y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        ZoomXY other = (ZoomXY) obj;
        return zoom == other.zoom && x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        int result = zoom;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }
}
