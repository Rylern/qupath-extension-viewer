package qupath.ext.viewer;

import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;

import java.util.List;

public class Volume extends Group {

    private final Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
    private double anchorX = 0;
    private double anchorY = 0;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;

    public Volume(Scene scene, int width, int height, int depth) {
        getChildren().add(new AmbientLight());
        setUpObjects(width, height, depth);
        setUpTransformations(scene);
    }

    private void setUpObjects(int width, int height, int depth) {
        Rectangle rectangle = new Rectangle(-width, -height, 2*width, 2*height);
        rectangle.getTransforms().add(new Rotate(60, Rotate.X_AXIS));
        rectangle.setFill(Color.BLUE);
        getChildren().add(rectangle);

        Box box = new Box(width, height, depth);
        box.setMaterial(new PhongMaterial(new Color(1, 0,0, .1)));
        getChildren().add(box);
    }

    private void setUpTransformations(Scene scene) {
        getTransforms().addAll(xRotate, yRotate);

        scene.setOnMousePressed(e -> onMousePressed(e.getSceneX(), e.getSceneY()));
        scene.setOnMouseDragged(e -> onMouseDragged(e.getSceneX(), e.getSceneY()));
        scene.addEventHandler(ScrollEvent.SCROLL, e -> onScroll(e.getDeltaY()));
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
        translateZProperty().set(getTranslateZ() - scroll / 10);
    }
}
