package si.um.feri.maprri.raster.classes.graphics;

public class HeightScaler {
    private double maxValue;
    private final float maxHeight;
    private final float minHeight;

    public HeightScaler(double maxValue, float maxHeight) {
        this.maxValue = maxValue;
        this.maxHeight = maxHeight;
        this.minHeight = 0.1f;
    }

    public float scale(double value) {
        if (maxValue <= 0) return minHeight;
        float scaled = (float) (Math.abs(value) / maxValue) * maxHeight;
        return Math.max(scaled, minHeight);
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getMaxValue() {
        return maxValue;
    }
}

