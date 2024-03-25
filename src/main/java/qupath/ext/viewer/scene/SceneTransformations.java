package qupath.ext.viewer.scene;

import javafx.scene.SubScene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

import java.util.List;

class SceneTransformations {

    private final Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
    private final Translate zTranslate = new Translate(0, 0, 0);
    private double anchorX = 0;
    private double anchorY = 0;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;

    public SceneTransformations(SubScene subScene) {
        subScene.setOnMousePressed(e -> onMousePressed(e.getSceneX(), e.getSceneY()));
        subScene.setOnMouseDragged(e -> onMouseDragged(e.getSceneX(), e.getSceneY()));
        subScene.addEventHandler(ScrollEvent.SCROLL, e -> onScroll(e.getDeltaY()));
    }

    public List<Transform> getTransforms() {
        return List.of(zTranslate, xRotate, yRotate);
    }

    private void onMousePressed(double xPosition, double yPosition) {
        anchorX = xPosition;
        anchorY = yPosition;
        anchorAngleX = xRotate.angleProperty().get();
        anchorAngleY = yRotate.angleProperty().get();
    }

    private void onMouseDragged(double xPosition, double yPosition) {
        xRotate.angleProperty().set(anchorAngleX - (anchorY - yPosition));
        yRotate.angleProperty().set(anchorAngleY + (anchorX - xPosition));
    }

    private void onScroll(double scroll) {
        zTranslate.setZ(zTranslate.getZ() - scroll / 10);
    }
}
