package si.um.feri.maprri.raster.classes;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class CustomPerspectiveCamera extends PerspectiveCamera {
    public CustomPerspectiveCamera(float fov, float width, float height) {
        super(fov, width, height);
    }
    public void setPositionAndDirection(float x, float y, float z, float yaw, float pitch) {
        float yawRad = MathUtils.degreesToRadians * yaw;
        float pitchRad = MathUtils.degreesToRadians * pitch;
        float fx = (float)( Math.cos(pitchRad) * Math.sin(yawRad) );
        float fy = (float)( Math.cos(pitchRad) * Math.cos(yawRad) );
        float fz = (float)( Math.sin(pitchRad) );
        Vector3 forward = new Vector3(fx, fy, fz).nor();

        this.position.set(x, y, z);
        Vector3 lookAt = new Vector3(x + forward.x, y + forward.y, z + forward.z);
        this.lookAt(lookAt);
        this.up.set(0, 0, 1);
        this.update();
    }
}
