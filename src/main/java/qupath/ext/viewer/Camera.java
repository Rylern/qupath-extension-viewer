package qupath.ext.viewer;

import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Camera extends PerspectiveCamera {

    private final Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate zRotate = new Rotate(0, Rotate.Z_AXIS);
    private final Translate zTranslate = new Translate(0, 0, -5);

    public Camera() {
        super(true);
        getTransforms().addAll(xRotate, yRotate, zRotate, zTranslate);
    }

    public void translate(boolean increase) {
        double step = increase ? 1 : -1;
        zTranslate.setZ(zTranslate.getZ() + step);
    }

    public void dragX(double value) {
        xRotate.setAngle(xRotate.getAngle() + value / 10);
    }

    public void dragY(double value) {
        yRotate.setAngle(yRotate.getAngle() + value / 10);
    }
}
