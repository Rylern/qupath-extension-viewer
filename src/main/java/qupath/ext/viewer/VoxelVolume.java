package qupath.ext.viewer;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;

public class VoxelVolume extends Group {

    private final Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
    private final Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
    private double anchorX = 0;
    private double anchorY = 0;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;

    public VoxelVolume(Scene scene, int width, int height, int depth) {
        getChildren().add(new AmbientLight());
        setUpVoxels(width, height, depth, 1);
        setUpTransformations(scene);
    }

    private void setUpVoxels(int width, int height, int depth, double scale) {
        for (int w=0; w<width; w++) {
            for (int h=0; h<height; h++) {
                for (int d=0; d<depth; d++) {
                    Box box = new Box(scale,scale, scale);
                    box.setMaterial(new PhongMaterial(new Color((double) w /width, (double) h /height, (double) d /depth, 1)));
                    box.translateXProperty().set(((double) -width /2*scale) + w*scale);
                    box.translateYProperty().set(((double) -height /2*scale) + h*scale);
                    box.translateZProperty().set(((double) -depth /2*scale) + d*scale);
                    getChildren().add(box);
                }
            }
        }
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
